package org.yats.trader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.messagebus.Config;
import org.yats.trader.examples.PositionServerLogic;
import org.yats.trading.PositionServer;
import org.yats.trading.ReceiptTest;

public class PositionServerLogicTest {


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
        sleepABit();

        assert(2==server2.getNumberOfPositions());
    }

    @BeforeMethod
    public void setUp() {
    }


    private void sleepABit() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


} // class
