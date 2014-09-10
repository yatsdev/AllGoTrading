package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.*;

public class LimitOrderBookTest implements IConsumePriceDataAndReceipt {

    @Test
    public void canMatchIntoEmptyBookCreatingMakerReceipts()
    {
        book.match(bid100At10);
        book.match(ask100At11);
        assert (2 == book.getSize());
    }

    @Test
    public void canCrossBookCreatingMakerOrderOnMatchingPrice()
    {
        book.match(ask100At11);
        book.match(bid200At12);
        assert (1 == book.getSize());
        assert (1 == book.getSize(BookSide.BID));
    }

    @Test
    public void canCancelAskOrder()
    {
        book.match(ask100At11);
        assert (1 == book.getSize());
        book.cancel(ask100At11.getOrderId());
        assert (0 == book.getSize());
    }

    @Test
    public void cancelNotExistingOrderDoesNothing()
    {
        book.match(bid100At10);
        assert (1 == book.getSize());
        book.cancel(ask100At11.getOrderId());
        assert (1 == book.getSize());
    }

    @Test
    public void cancelBidOrderLeavingAsk()
    {
        book.match(bid100At10);
        book.match(ask100At11);
        assert (2 == book.getSize());
        book.cancel(bid100At10.getOrderId());
        assert (1 == book.getSize(BookSide.ASK));
    }

    @Test
    public void cancelBidOrderResultsInReceiptWithEndstateTrue()
    {
        book.match(bid100At10);
        assert (1 == receiptCounter);

        book.cancel(bid100At10.getOrderId());
        assert (2 == receiptCounter);
        assert(lastReceipt.isEndState());
        assert(!lastReceipt.isRejection());
    }


    @BeforeMethod
    public void setUp() {
        book = new LimitOrderBook(ProductTest.TEST_PRODUCT1_ID, this);
        bid100At10 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(10))
                .withSize(Decimal.fromDouble(100));
        bid200At12 = new OrderNew()
                .withBookSide(BookSide.BID)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(12))
                .withSize(Decimal.fromDouble(200));
        ask100At11 = new OrderNew()
                .withBookSide(BookSide.ASK)
                .withInternalAccount(ProductTest.ACCOUNT1)
                .withProductId(ProductTest.TEST_PRODUCT1_ID)
                .withLimit(Decimal.fromDouble(11))
                .withSize(Decimal.fromDouble(100));
        receiptCounter=0;
        lastReceipt=null;
    }


    @Override
    public void onReceipt(Receipt receipt) {
        receiptCounter++;
        lastReceipt=receipt;
    }

    @Override
    public void onPriceData(PriceData priceData) {

    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    LimitOrderBook book;
    OrderNew bid100At10;
    OrderNew bid200At12;
    OrderNew ask100At11;
    Receipt lastReceipt;
    int receiptCounter;
} // class
