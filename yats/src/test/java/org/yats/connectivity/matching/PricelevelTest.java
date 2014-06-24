package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class PricelevelTest implements IConsumeReceipt {


    @Test
    public void canAddOrder()
    {
        level.add(orderAsk1);
        level.match(orderBid1);
        assert(2==receipts);
    }


    @BeforeMethod
    public void setUp() {
        level = new PriceLevel(this);
        orderBid1 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(10))
                .withSize(Decimal.fromDouble(100));
        orderAsk1 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(11))
                .withSize(Decimal.fromDouble(100));
        receipts=0;
    }


    @Override
    public void onReceipt(Receipt receipt) {
        receipts++;
    }

    int receipts;

    PriceLevel level;
    OrderNew orderBid1;
    OrderNew orderAsk1;

} // class
