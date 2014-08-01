package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OrderBookTest {

    @Test
    public void canConvertToAndFromCSV() {
        String csv = book.toStringCSV();
        OrderBook csvBook = OrderBook.fromStringCSV(csv);
        String csv2 = csvBook.toStringCSV();
        assert(csv.compareTo(csv2)==0);
    }

    @BeforeMethod
    public void setUp() {
        book = new OrderBook();
        book.addBid("10", "22");
        book.addAsk("11", "23");
    }

    private OrderBook book;

} // class
