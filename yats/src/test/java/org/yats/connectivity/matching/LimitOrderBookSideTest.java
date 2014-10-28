package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.trading.*;

public class LimitOrderBookSideTest implements IConsumeReceipt {


    @Test(groups = { "inMemory" })
    public void canAddOrder()
    {
        bookBid.add(bid100At10);
        bookBid.add(bid200At9);
        assert (2 == bookBid.getSize());
    }

    @Test(groups = { "inMemory" })
    public void canFindFirstBidRow()
    {
        bookBid.add(bid100At10);
        bookBid.add(bid200At9);
        Decimal bidFront = bookBid.getFrontRowPrice();
        assert(bid100At10.getLimit() == bidFront);
    }

    @Test(groups = { "inMemory" })
    public void canFindFirstAskRow()
    {
        bookAsk.add(ask100At11);
        bookAsk.add(ask300At12);
        assert(ask100At11.getLimit() == bookAsk.getFrontRowPrice());
    }

    @Test(groups = { "inMemory" })
    public void canMatchBidOrderIntoAskSide()
    {
        bookAsk.add(ask100At11);
        bookAsk.add(ask300At12);
        bookAsk.match(bid200At12);
        assert(tradedAccount1==-200);
        assert(tradedAccount2==200);
    }

    @Test(groups = { "inMemory" })
    public void canMatchAndHaveResidual()
    {
        bookAsk.add(ask100At11);
        bookAsk.match(bid200At12);
        assert(tradedAccount1==-100);
        assert(tradedAccount2==100);
        assert(lastBidReceipt.getResidualSize().isEqualTo(Decimal.fromDouble(100)));
    }

    @Test(groups = { "inMemory" })
    public void canCreateOfferBookSideForBidSide()
    {
        assert(bookBid.toOfferBookSide(10).toStringCSV().length()==0);
        assert(bookBid.toOfferBookSide(10).size()==0);
        bookBid.add(bid100At10);
        bookBid.add(bid200At9);
        bookBid.add(bid200At12);
        bookBid.add(bid50At8);
        OfferBookSide bidSide = bookBid.toOfferBookSide(3);
        String bidSideString = bidSide.toStringCSV();
        String expected = bid200At12.toBookRowCSV() + OfferBookSide.CSV_SEPARATOR
                        + bid100At10.toBookRowCSV() + OfferBookSide.CSV_SEPARATOR
                        + bid200At9.toBookRowCSV();
        assert(bidSideString.compareTo(expected)==0);
    }

    @Test(groups = { "inMemory" })
    public void canCreateOfferBookSideForAskSide()
    {
        bookAsk.add(ask100At11);
        bookAsk.add(ask10At9_80);
        bookAsk.add(ask15At10_10);
        bookAsk.add(ask300At12);
        OfferBookSide askSide = bookAsk.toOfferBookSide(3);
        assert(askSide.size()==3);
        String askSideString = askSide.toStringCSV();
        String expected = ask10At9_80.toBookRowCSV() + OfferBookSide.CSV_SEPARATOR
                        + ask15At10_10.toBookRowCSV() + OfferBookSide.CSV_SEPARATOR
                        + ask100At11.toBookRowCSV();
        assert(askSideString.compareTo(expected)==0);
    }

    @BeforeMethod(groups = { "inMemory" })
    public void setUp() {
        bookBid = new LimitOrderBookSide(BookSide.BID, this);
        bookAsk = new LimitOrderBookSide(BookSide.ASK, this);
        bid100At10 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(10))
                .withSize(Decimal.fromDouble(100));
        bid200At9 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(9))
                .withSize(Decimal.fromDouble(200));
        bid200At12 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT2)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(200));
        bid50At8 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT2)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(8))
                .withSize(Decimal.fromDouble(50));

        ask100At11 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(11))
                .withSize(Decimal.fromDouble(100));
        ask300At12 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(300));
        ask10At9_80 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(9.8))
                .withSize(Decimal.fromDouble(10));
        ask15At10_10 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(10.1))
                .withSize(Decimal.fromDouble(15));
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
    LimitOrderBookSide bookBid;
    LimitOrderBookSide bookAsk;
    OrderNew bid100At10;
    OrderNew bid200At9;
    OrderNew bid200At12;
    OrderNew bid50At8;
    OrderNew ask100At11;
    OrderNew ask300At12;
    OrderNew ask15At10_10;
    OrderNew ask10At9_80;
    int tradedAccount1;
    int tradedAccount2;

} // class
