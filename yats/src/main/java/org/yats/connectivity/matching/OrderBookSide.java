package org.yats.connectivity.matching;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;
import org.yats.trading.BookSide;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.OrderNew;

import java.util.SortedMap;
import java.util.TreeMap;

public class OrderBookSide {

    public void match(OrderNew order) {
        try {
            doMatch(order);
        } catch(CommonExceptions.ContainerEmptyException e) {
        }
    }

    public void doMatch(OrderNew order) {
        do {
            Decimal frontRowPrice = getFrontRowPrice();
            if(!order.isExecutingWith(frontRowPrice)) return;
            PriceLevel frontRow = book.get(frontRowPrice);
            frontRow.match(order);
        } while(true);
    }

    public void add(OrderNew order) {
        Decimal limit = order.getLimit();
        PriceLevel row = book.containsKey(limit) ? book.get(limit) : new PriceLevel();
        row.add(order);
        book.put(limit, row);
    }

    public Decimal getFrontRowPrice() {
        if(isEmpty()) throw new CommonExceptions.ContainerEmptyException("book is empty!");
        Decimal frontRowPrice = side==BookSide.BID ? book.lastKey() : book.firstKey();
        return frontRowPrice;
    }

    public boolean isEmpty() {
        return book.isEmpty();
    }

    public int getSize() {
        return book.size();
    }

    public OrderBookSide(BookSide side, IConsumeReceipt _receiptConsumer) {
        this.side = side;
        receiptConsumer = _receiptConsumer;

        book = new TreeMap<Decimal, PriceLevel>();

    }




    private BookSide side;
    private IConsumeReceipt receiptConsumer;

    private SortedMap<Decimal,PriceLevel> book;


} // class
