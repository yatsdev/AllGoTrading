package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
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

    public void cancel(UniqueId orderId) {
        book[0].cancel(orderId);
        book[1].cancel(orderId);
        sendMarketData();
    }


    public boolean isOrderInBooks(UniqueId orderId){
        boolean inBookBid =book[0].isOrderIdInBook(orderId.toString());
        boolean inBookAsk =book[1].isOrderIdInBook(orderId.toString());
        return inBookBid || inBookAsk;
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

            if(bidSize.isZero() || askSize.isZero()) {
                return;
            }

            Decimal last = lastReceipt != null ? lastReceipt.getPrice() : Decimal.ZERO;
            Decimal lastSize = lastReceipt != null ? lastReceipt.getCurrentTradedSize() : Decimal.ZERO;
            MarketData m = new MarketData(Tool.getUTCTimestamp(), productId, bid, ask, last, bidSize, askSize, lastSize);
            OfferBook offerBook = new OfferBook(
                    book[0].toOfferBookSide(10),
                    book[1].toOfferBookSide(10));
            m.setBook(offerBook);
            consumer.onMarketData(m);
        } catch(CommonExceptions.ContainerEmptyException e) {
            // todo: enable sending even if only half the book is filled or totally empty?
        }
    }

    public int getSize(BookSide _side) {
        return book[_side.toIndex()].getSize();
    }

    public int getSize() {
        return book[0].getSize() + book[1].getSize();
    }


    public LimitOrderBook(String _productId, IConsumeMarketDataAndReceipt _consumer)
    {
        productId=_productId;
        consumer = _consumer;
        book = new LimitOrderBookSide[2];
        book[0] = new LimitOrderBookSide(BookSide.BID, this);
        book[1] = new LimitOrderBookSide(BookSide.ASK, this);
        frontRowPrice = new Decimal[2];
        frontRowPrice[0] = null;
        frontRowPrice[1] = null;
    }

    Receipt lastBidReceipt;
    Receipt lastAskReceipt;
    Receipt lastReceipt;
    LimitOrderBookSide book[];
    private Decimal frontRowPrice[];
    IConsumeMarketDataAndReceipt consumer;
    String productId;


} // class
