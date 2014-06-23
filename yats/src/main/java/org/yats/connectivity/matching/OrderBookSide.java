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
        Decimal frontRowPrice = getFrontRowPrice();
        if(!order.isExecutingWith(frontRowPrice)) return;
        OrderList frontRow = book.get(frontRowPrice);


    }

    public void add(OrderNew order) {
        Decimal limit = order.getLimit();
        OrderList row = book.containsKey(limit) ? book.get(limit) : new OrderList();
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

        book = new TreeMap<Decimal, OrderList>();

    }




    private BookSide side;
    private IConsumeReceipt receiptConsumer;

    private SortedMap<Decimal,OrderList> book;


} // class
