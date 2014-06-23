package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class LimitOrderBookTest implements IConsumeReceipt {

//    @Test
//    public void canConvertPositionRequest()
//    {
//        bookBid.add(orderBid1);
//        bookBid.add(orderAsk1);
//        assert (2 == bookBid.getSize());
//    }

    @BeforeMethod
    public void setUp() {
        book = new LimitOrderBook(this);
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
    }


    @Override
    public void onReceipt(Receipt receipt) {

    }

    LimitOrderBook book;
    OrderNew orderBid1;
    OrderNew orderAsk1;
} // class
