package org.yats.connectivity.matching;

import org.yats.common.Decimal;
import org.yats.trading.BookSide;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;

import java.util.SortedMap;
import java.util.TreeMap;

public class LimitOrderBook {

    public void add(OrderNew orderNew) {
        BookSide side = orderNew.getBookSide();
        if(frontRowPrice[side.toIndex()]==null){

        }
    }

    public int getSize() {
        return size;
    }

    private int size;

    public LimitOrderBook(IConsumeReceipt _receiptConsumer)
    {
        receiptConsumer = _receiptConsumer;
        book = new TreeMap<Decimal, PriceLevel>();
        frontRowPrice = new Decimal[2];
        frontRowPrice[0] = null;
        frontRowPrice[1] = null;
    }

    private SortedMap<Decimal,PriceLevel> book;
    private Decimal frontRowPrice[];
    IConsumeReceipt receiptConsumer;


} // class
