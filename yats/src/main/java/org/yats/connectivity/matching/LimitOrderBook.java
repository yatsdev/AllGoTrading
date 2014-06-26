package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.common.Tool;
import org.yats.trading.*;

public class LimitOrderBook implements IConsumeReceipt {

    public void match(OrderNew orderNew) {
        Receipt takerReceipt = orderNew.createReceiptDefault();
        int oppositeIndex = takerReceipt.getBookSide().toOpposite().toIndex();
        book[oppositeIndex].match(takerReceipt);
        int index = takerReceipt.getBookSide().toIndex();
        if(!takerReceipt.isEndState()) book[index].add(takerReceipt);
        sendMarketData();
    }


    @Override
    public void onReceipt(Receipt receipt) {
        lastReceipt=receipt;
        consumer.onReceipt(receipt);
    }

    //todo: only send if changed
    private void sendMarketData() {
        try {
            Decimal bid = book[0].getFrontRowPrice();
            Decimal bidSize = book[0].getFrontRowSize();
            Decimal ask = book[1].getFrontRowPrice();
            Decimal askSize = book[1].getFrontRowSize();

            Decimal last = lastReceipt != null ? lastReceipt.getPrice() : Decimal.ZERO;
            Decimal lastSize = lastReceipt != null ? lastReceipt.getCurrentTradedSize() : Decimal.ZERO;
            MarketData m = new MarketData(Tool.getUTCTimestamp(), "pid", bid, ask, last, bidSize, askSize, lastSize);
            consumer.onMarketData(m);
        } catch(CommonExceptions.ContainerEmptyException e) {
            // todo: enable sending even if only half the book is filled?
        }
    }

    public int getSize(BookSide _side) {
        return book[_side.toIndex()].getSize();
    }

    public int getSize() {
        return book[0].getSize() + book[1].getSize();
    }


    public LimitOrderBook(IConsumeMarketDataAndReceipt _receiptConsumer)
    {
        consumer = _receiptConsumer;
        book = new OrderBookSide[2];
        book[0] = new OrderBookSide(BookSide.BID, this);
        book[1] = new OrderBookSide(BookSide.ASK, this);
        frontRowPrice = new Decimal[2];
        frontRowPrice[0] = null;
        frontRowPrice[1] = null;
    }

    Receipt lastBidReceipt;
    Receipt lastAskReceipt;
    Receipt lastReceipt;
    OrderBookSide book[];
    private Decimal frontRowPrice[];
    IConsumeMarketDataAndReceipt consumer;



} // class
