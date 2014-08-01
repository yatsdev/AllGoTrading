package org.yats.trading;

import java.util.Vector;

public class OrderBook {

    @Override
    public String toString() {
        return "OrderBook{" +
                "bookBid=" + bookBid +
                ", bookAsk=" + bookAsk +
                '}';
    }

    public static OrderBook fromStringCSV(String csv) {
        OrderBook book = new OrderBook();
        String[] booksArray = csv.split("|");
        {
            String[] rowsBid = booksArray[0].split(";");
            for(String bid : rowsBid) {
                BookRow r = BookRow.fromStringCSV(bid);
                book.addBid(r);
            }
        }

        {
            String[] rowsAsk = booksArray[1].split(";");
            for(String ask : rowsAsk) {
                BookRow r = BookRow.fromStringCSV(ask);
                book.addAsk(r);
            }
        }
        return book;
    }

    public void addBid(String size, String price) {
        addBid(new BookRow(size, price));
    }

    public void addBid(BookRow r) {
        bookBid.add(r);
    }

    public void addAsk(String size, String price) {
        addAsk(new BookRow(size, price));
    }

    public void addAsk(BookRow r) {
        bookAsk.add(r);
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();

        boolean firstBid = true;
        for(BookRow r : bookBid) {
            if(!firstBid) b.append(";");
            b.append(r.toStringCSV());
        }
        b.append("|");

        boolean firstAsk = true;
        for(BookRow r : bookAsk) {
            if(!firstAsk) b.append(";");
            b.append(r.toStringCSV());
        }

        return b.toString();
    }

    public OrderBook() {
        bookBid=new Vector<BookRow>();
        bookAsk=new Vector<BookRow>();
    }

private Vector<BookRow> bookBid;
private Vector<BookRow> bookAsk;

} // class
