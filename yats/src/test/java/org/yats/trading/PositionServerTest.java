package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.ThreadTool;
import org.yats.common.UniqueId;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trader.examples.PositionServerLogic;

public class PositionServerTest {

    @Test
    public void canProcessReceipts() {
        assert (positionServer.getNumberOfReceipts() == 5);
    }

    @Test
    public void canCalculateCurrentPositionOverAllInternalAccounts() {
        Position p = positionServer.getPositionForAllAccounts(ProductTest.PRODUCT1.getProductId());
        assert (p.isSize(+1 +1 +1 +9 -2));
    }

    @Test
    public void canCalculatePositionForInternalAccount()
    {
        Position positionAccount1 = positionServer.getAccountPosition(new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT1, ProductTest.PRODUCT1.getProductId()));
        assert (positionAccount1.isSize(+1 +1 +1 -2));
        Position positionAccount2 = positionServer.getAccountPosition(new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT2, ProductTest.PRODUCT1.getProductId()));
        assert (positionAccount2.isSize(9));
    }

    @Test
    public void canCalculatePositionForInternalAccountWithSnapshot() {
        positionServer.addPositionSnapshot(positionSnapshot);
        Position positionWithSnapshot = positionServer.getAccountPosition(positionRequest1);
        assert(positionWithSnapshot.isSize(+1 + 1 + 1 -2 +10));
    }

//    @Test
//    public void canStorePositionSnapshot() {
//        Config c = Config.DEFAULT_FOR_TESTS;
//        c.setStorePositionsToDisk(true);
//        c.setListeningForReceipts(true);
//        PositionServerLogic logic = new PositionServerLogic(c);
//        logic.setPositionStorage(positionStorage);
//        senderReceipts = new Sender<ReceiptMsg>(c.getExchangeReceipts(),c.getServerIP());
//        ReceiptMsg m = ReceiptMsg.fromReceipt(ReceiptTest.RECEIPT1);
//        senderReceipts.publish(m.getTopic(), m);
//        ThreadTool.sleepABit();
//
//        assert(1==positionStorage.getSnapshotCount());
//    }


    @BeforeMethod
    public void setUp() {
        positionServer = new PositionServer();
        processReceipts();
        positionSnapshot = new PositionSnapshot();
        AccountPosition p;
        p = new AccountPosition(ProductTest.PRODUCT1.getProductId(), ReceiptTest.INTERNAL_ACCOUNT1, Decimal.fromDouble(10));
        positionSnapshot.add(p);
        positionRequest1 = new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT1, ProductTest.PRODUCT1.getProductId());
    }

    private void processReceipts() {
        positionServer.onReceipt(ReceiptTest.RECEIPT1);
        positionServer.onReceipt(ReceiptTest.RECEIPT2);
        positionServer.onReceipt(ReceiptTest.RECEIPT3);
        positionServer.onReceipt(ReceiptTest.RECEIPT4);
        positionServer.onReceipt(ReceiptTest.RECEIPT5);
    }


    private PositionServer positionServer;

//    private static String INTERNAL_ACCOUNT1 = "intAccount1";
//    private static String INTERNAL_ACCOUNT2 = "intAccount2";
    PositionSnapshot positionSnapshot;
    PositionRequest positionRequest1;

}
