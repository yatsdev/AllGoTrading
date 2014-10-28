package org.yats.trader;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trader.examples.server.PositionServerMain;
import org.yats.trading.*;

import java.util.ArrayList;

public class PositionServerLogicTest {

    //final Logger log = LoggerFactory.getLogger(PositionServerLogicTest.class);

    @Test(groups = { "integration", "inMemory" })
    public void canInitializeOnePositionServerFromAnother()
    {
        PositionServerMain logic1 = new PositionServerMain(prop);
        PositionServer server1 = new PositionServer();
        server1.setProductList(productList);
        logic1.setPositionServer(server1);
        server1.onReceipt(ReceiptTest.RECEIPT1);
        server1.onReceipt(ReceiptTest.RECEIPT4);
        logic1.startRequestListener();

        PositionServerMain logic2 = new PositionServerMain(prop);
        PositionServer server2 = new PositionServer();
        logic2.setPositionServer(server2);

        logic2.startSnapshotListener();
        logic2.requestPositionSnapshotFromPositionServer();
        Tool.sleepABit();

        assert(4==server2.getNumberOfPositions());

        logic1.close();
        logic2.close();
    }

    @Test(groups = { "integration", "inMemory" })
    public void canStorePositionSnapshot() {

        config.setStorePositionsToDisk(true);
        config.setListeningForReceipts(true);
        PositionServerMain logic = new PositionServerMain(prop);
        logic.setProductList(productList);
        logic.setPositionStorage(positionStorage);
        ReceiptMsg m = ReceiptMsg.fromReceipt(ReceiptTest.RECEIPT1);
        senderReceipts.publish(m.getTopic(), m);
        Tool.sleepABit();

        assert(1==positionStorage.getSnapshotCount());
        logic.close();
    }


    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setUp() {
        productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        prop = Config.createTestProperties();
        config = Config.fromProperties(prop);
        senderReceipts = new Sender<ReceiptMsg>(config.getExchangeReceipts(),config.getServerIP());

        positionStorage = new PositionSnapshotStorageMem();
    }

    @AfterMethod
    public void tearDown() {
        senderReceipts.close();
    }

    ProductList productList;
    private PositionSnapshotStorageMem positionStorage;
    private Sender<ReceiptMsg> senderReceipts;
    private Config config;
    private IProvideProperties prop;

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
    }

} // class
