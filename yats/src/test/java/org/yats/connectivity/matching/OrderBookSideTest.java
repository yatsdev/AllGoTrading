package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class OrderBookSideTest implements IConsumeReceipt {


    @Test
    public void canAddOrder()
    {
        bookBid.add(orderBid1);
        bookBid.add(orderBid2);
        assert (2 == bookBid.getSize());
    }

    @Test
    public void canFindFirstBidRow()
    {
        bookBid.add(orderBid1);
        bookBid.add(orderBid2);
        assert(orderBid1.getLimit() == bookBid.getFrontRowPrice());
    }

    @Test
    public void canFindFirstAskRow()
    {
        bookAsk.add(orderAsk1);
        bookAsk.add(orderAsk2);
        assert(orderAsk1.getLimit() == bookAsk.getFrontRowPrice());
    }

    @Test
    public void canMatchBidOrderIntoAskSide()
    {
        bookAsk.add(orderAsk1);
        bookAsk.add(orderAsk2);
        bookAsk.match(orderBid3);
        assert(tradedAccount1==-200);
        assert(tradedAccount2==200);
    }



    @BeforeMethod
    public void setUp() {
        bookBid = new OrderBookSide(BookSide.BID, this);
        bookAsk = new OrderBookSide(BookSide.ASK, this);
        orderBid1 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(10))
                .withSize(Decimal.fromDouble(100));
        orderBid2 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(9))
                .withSize(Decimal.fromDouble(200));
        orderBid3 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT2)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(200));

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
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(300));
        tradedAccount1=0;
        tradedAccount2=0;
    }

    @Override
    public void onReceipt(Receipt receipt) {
        if(receipt.getInternalAccount().compareTo(ProductTest.ACCOUNT1)==0) {
            tradedAccount1+=receipt.getCurrentTradedSizeSigned().toInt();
        } else
        {
            tradedAccount2+=receipt.getCurrentTradedSizeSigned().toInt();
        }
    }

    OrderBookSide bookBid;
    OrderBookSide bookAsk;
    OrderNew orderBid1;
    OrderNew orderBid2;
    OrderNew orderBid3;
    OrderNew orderAsk1;
    OrderNew orderAsk2;
    int tradedAccount1;
    int tradedAccount2;

} // class
