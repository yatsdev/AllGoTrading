package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class PricelevelTest implements IConsumeReceipt {


    @Test(expectedExceptions = CommonExceptions.ContainerEmptyException.class)
    public void doesNotMatchNotCrossingOrders()
    {
        level.add(orderAsk1);
        level.match(orderBid1);
    }

    @Test
    public void matchesCrossingOrder()
    {
        level.add(orderAsk1);
        level.match(orderBid2);
        assert(2==receipts);
    }

    @Test
    public void matchesMultipleOrdersLeavesRest()
    {
        level.add(orderAsk1);
        level.add(orderAsk2);
        level.match(orderBid3);
        assert(4==receipts);
        assert(lastBidReceipt.isEndState());
        assert(!lastAskReceipt.isEndState());
        assert(lastAskReceipt.getResidualSize().isEqualTo(Decimal.fromDouble(150)));
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
        orderAsk2 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(11))
                .withSize(Decimal.fromDouble(200));
        orderBid2 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(100));
        orderBid3 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(150));

        receipts=0;
    }


    @Override
    public void onReceipt(Receipt receipt) {
        receipts++;
        if(receipt.isBookSide(BookSide.BID)) {
            lastBidReceipt=receipt;
        } else
        {
            lastAskReceipt=receipt;
        }
    }

    int receipts;

    PriceLevel level;
    OrderNew orderBid1;
    OrderNew orderBid2;
    OrderNew orderBid3;
    OrderNew orderAsk1;
    OrderNew orderAsk2;
    Receipt lastBidReceipt;
    Receipt lastAskReceipt;

} // class
