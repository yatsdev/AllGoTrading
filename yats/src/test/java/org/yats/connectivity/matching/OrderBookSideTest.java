package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class OrderBookSideTest implements IConsumeReceipt {


    @Test
    public void canAddOrder()
    {
        bookBid.add(bid100At10);
        bookBid.add(bid200At9);
        assert (2 == bookBid.getSize());
    }

    @Test
    public void canFindFirstBidRow()
    {
        bookBid.add(bid100At10);
        bookBid.add(bid200At9);
        assert(bid100At10.getLimit() == bookBid.getFrontRowPrice());
    }

    @Test
    public void canFindFirstAskRow()
    {
        bookAsk.add(ask100At11);
        bookAsk.add(ask300At12);
        assert(ask100At11.getLimit() == bookAsk.getFrontRowPrice());
    }

    @Test
    public void canMatchBidOrderIntoAskSide()
    {
        bookAsk.add(ask100At11);
        bookAsk.add(ask300At12);
        bookAsk.match(bid200At12);
        assert(tradedAccount1==-200);
        assert(tradedAccount2==200);
    }

    @Test
    public void canMatchAndHaveResidual()
    {
        bookAsk.add(ask100At11);
        bookAsk.match(bid200At12);
        assert(tradedAccount1==-100);
        assert(tradedAccount2==100);
        assert(lastBidReceipt.getResidualSize().isEqualTo(Decimal.fromDouble(100)));
    }

    @BeforeMethod
    public void setUp() {
        bookBid = new OrderBookSide(BookSide.BID, this);
        bookAsk = new OrderBookSide(BookSide.ASK, this);
        bid100At10 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(10))
                .withSize(Decimal.fromDouble(100));
        bid200At9 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(9))
                .withSize(Decimal.fromDouble(200));
        bid200At12 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT2)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(200));

        ask100At11 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.PRODUCT1.getProductId())
                .withLimit(Decimal.fromDouble(11))
                .withSize(Decimal.fromDouble(100));
        ask300At12 = new OrderNew()
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
        if(receipt.isBookSide(BookSide.BID)) lastBidReceipt = receipt;
        if(receipt.isBookSide(BookSide.ASK)) lastAskReceipt = receipt;
        if(receipt.getInternalAccount().compareTo(ProductTest.ACCOUNT1)==0) {
            tradedAccount1+=receipt.getCurrentTradedSizeSigned().toInt();
        } else
        {
            tradedAccount2+=receipt.getCurrentTradedSizeSigned().toInt();
        }
    }

    Receipt lastBidReceipt;
    Receipt lastAskReceipt;
    OrderBookSide bookBid;
    OrderBookSide bookAsk;
    OrderNew bid100At10;
    OrderNew bid200At9;
    OrderNew bid200At12;
    OrderNew ask100At11;
    OrderNew ask300At12;
    int tradedAccount1;
    int tradedAccount2;

} // class
