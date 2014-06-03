package org.yats.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trader.examples.PositionServerLogic;
import org.yats.trading.IStorePositionSnapshots;
import org.yats.trading.PositionServer;
import org.yats.trading.PositionSnapshot;
import org.yats.trading.ReceiptTest;

import java.util.ArrayList;

public class PositionServerLogicTest {

    final Logger log = LoggerFactory.getLogger(PositionServerLogicTest.class);

    @Test
    public void canInitializeOnePositionServerFromAnother()
    {
        PositionServerLogic logic1 = new PositionServerLogic(Config.DEFAULT_FOR_TESTS);
        PositionServer server1 = new PositionServer();
        logic1.setPositionServer(server1);
        server1.onReceipt(ReceiptTest.RECEIPT1);
        server1.onReceipt(ReceiptTest.RECEIPT4);
        logic1.startRequestListener();

        PositionServerLogic logic2 = new PositionServerLogic(Config.DEFAULT_FOR_TESTS);
        PositionServer server2 = new PositionServer();
        logic2.setPositionServer(server2);

        logic2.startSnapshotListener();
        logic2.requestPositionSnapshotFromPositionServer();
        Tool.sleepABit();

        assert(2==server2.getNumberOfPositions());
    }

    @Test
    public void canStorePositionSnapshot() {
        Config c = Config.DEFAULT_FOR_TESTS;
        c.setStorePositionsToDisk(true);
        c.setListeningForReceipts(true);
        PositionServerLogic logic = new PositionServerLogic(c);
        logic.setPositionStorage(positionStorage);
        senderReceipts = new Sender<ReceiptMsg>(c.getExchangeReceipts(),c.getServerIP());
        ReceiptMsg m = ReceiptMsg.fromReceipt(ReceiptTest.RECEIPT1);
        senderReceipts.publish(m.getTopic(), m);
        Tool.sleepABit();

        assert(1==positionStorage.getSnapshotCount());
    }


    @BeforeMethod
    public void setUp() {
        positionStorage = new PositionSnapshotStorageMem();
    }


    private PositionSnapshotStorageMem positionStorage;
    private Sender<ReceiptMsg> senderReceipts;


    private class PositionSnapshotStorageMem implements IStorePositionSnapshots  {
        @Override
        public void store(PositionSnapshot positionSnapshot) {
            positionSnapshots.add(positionSnapshot);

        }
        @Override
        public PositionSnapshot readLast() {
            if(positionSnapshots.size()<1) return new PositionSnapshot();
            return positionSnapshots.get(positionSnapshots.size()-1);
        }
        public int getSnapshotCount() {return positionSnapshots.size();}

        private PositionSnapshotStorageMem() {
            positionSnapshots=new ArrayList<PositionSnapshot>();
        }
        ArrayList<PositionSnapshot> positionSnapshots;
    };

} // class
