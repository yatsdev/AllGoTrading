package org.yats.connectivity.matching;

import org.yats.common.Decimal;
import org.yats.trading.BookSide;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;
import org.yats.trading.Receipt;

public class LimitOrderBook implements IConsumeReceipt {

    public void match(OrderNew orderNew) {
        Receipt takerReceipt = orderNew.createReceiptDefault();
        int oppositeIndex = takerReceipt.getBookSide().toOpposite().toIndex();
        book[oppositeIndex].match(takerReceipt);
        int index = takerReceipt.getBookSide().toIndex();
        if(!takerReceipt.isEndState()) book[index].add(takerReceipt);
    }

    @Override
    public void onReceipt(Receipt receipt) {

        receiptConsumer.onReceipt(receipt);
    }

    //    public void add(OrderNew orderNew) {
//        BookSide side = orderNew.getBookSide();
//        if(frontRowPrice[side.toIndex()]==null){
//
//        }
//    }

    public int getSize(BookSide _side) {
        return book[_side.toIndex()].getSize();
    }

    public int getSize() {
        return book[0].getSize() + book[1].getSize();
    }


    public LimitOrderBook(IConsumeReceipt _receiptConsumer)
    {
        receiptConsumer = _receiptConsumer;
        book = new OrderBookSide[2];
        book[0] = new OrderBookSide(BookSide.BID, this);
        book[1] = new OrderBookSide(BookSide.ASK, this);
        frontRowPrice = new Decimal[2];
        frontRowPrice[0] = null;
        frontRowPrice[1] = null;
    }

    Receipt lastBidReceipt;
    Receipt lastAskReceipt;
    OrderBookSide book[];
    private Decimal frontRowPrice[];
    IConsumeReceipt receiptConsumer;



} // class
