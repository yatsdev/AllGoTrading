package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;

public class PositionServerTest {

    @Test
    public void canProcessReceipts() {
        assert (positionServer.getNumberOfReceipts() == 5);
    }

    @Test
    public void canCalculateCurrentPositionOverAllInternalAccounts() {
        Position p = positionServer.getPositionForAllAccounts(product1.getProductId());
        assert (p.isSize(+1 +1 +1 +9 -2));
    }

    @Test
    public void canCalculatePositionForInternalAccount()
    {
        Position positionAccount1 = positionServer.getAccountPosition(new PositionRequest(INTERNAL_ACCOUNT1, product1.getProductId()));
        assert (positionAccount1.isSize(+1 +1 +1 -2));
        Position positionAccount2 = positionServer.getAccountPosition(new PositionRequest(INTERNAL_ACCOUNT2, product1.getProductId()));
        assert (positionAccount2.isSize(9));
    }

    @Test
    public void canCalculatePositionForInternalAccountWithSnapshot() {
        positionServer.addPositionSnapshot(positionSnapshot);
        Position positionWithSnapshot = positionServer.getAccountPosition(positionRequest1);
        assert(positionWithSnapshot.isSize(+1 + 1 + 1 -2 +10));
    }

    @BeforeMethod
    public void setUp() {
        positionServer = new PositionServer();
        product1 = new Product("product1", "sym1", "exch1");
        receipt1 = Receipt.create()
                .withOrderId(UniqueId.createFromString("1"))
                .withProductId(product1.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.ONE)
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt2 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProductId(product1.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.ONE)
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ONE)
                .withBookSide(BookSide.BID)
        ;
        receipt3 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProductId(product1.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.fromDouble(2))
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt4 = Receipt.create()
                .withOrderId(UniqueId.createFromString("4"))
                .withProductId(product1.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT2)
                .withCurrentTradedSize(Decimal.fromDouble(9))
                .withTotalTradedSize(Decimal.fromDouble(9))
                .withPrice(Decimal.fromDouble(87))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt5 = Receipt.create()
                .withOrderId(UniqueId.createFromString("5"))
                .withProductId(product1.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.fromDouble(2))
                .withTotalTradedSize(Decimal.fromDouble(2))
                .withPrice(Decimal.fromDouble(48))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.ASK)
        ;

        processReceipts();
        positionSnapshot = new PositionSnapshot();
        AccountPosition p = new AccountPosition(product1.getProductId(), Decimal.fromDouble(10), INTERNAL_ACCOUNT1);
        positionSnapshot.add(p);
        positionRequest1 = new PositionRequest(INTERNAL_ACCOUNT1, product1.getProductId());
    }

    private void processReceipts() {
        positionServer.onReceipt(receipt1);
        positionServer.onReceipt(receipt2);
        positionServer.onReceipt(receipt3);
        positionServer.onReceipt(receipt4);
        positionServer.onReceipt(receipt5);
    }


    private PositionServer positionServer;

    private static String INTERNAL_ACCOUNT1 = "intAccount1";
    private static String INTERNAL_ACCOUNT2 = "intAccount2";
    PositionSnapshot positionSnapshot;
    Product product1;
    Receipt receipt1;
    Receipt receipt2;
    Receipt receipt3;
    Receipt receipt4;
    Receipt receipt5;
    PositionRequest positionRequest1;

}
