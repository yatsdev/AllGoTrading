package org.yats.trading;

import java.util.Vector;

public class OfferBookSide { // implements List<BookRow> {

    public static final String CSV_SEPARATOR = ";";

    public int size() {
        return bookHalf.size();
    }

    public BookRow getRow(int index) {
        return bookHalf.elementAt(index);
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();
        boolean firstRow = true;
        for(BookRow r : bookHalf) {
            if(!firstRow) {
                b.append(CSV_SEPARATOR);
            }
            b.append(r.toStringCSV());
            firstRow=false;
        }
        return b.toString();
    }

    public static OfferBookSide fromStringCSV(String s, BookSide _side) {
        OfferBookSide bookHalf = new OfferBookSide(_side);
        String[] rows = s.split(CSV_SEPARATOR);
        for(String r : rows) {
            BookRow row = BookRow.fromStringCSV(r);
            bookHalf.add(row);
        }
        return bookHalf;
    }

    public void add(BookRow row) {
        bookHalf.add(row);
    }

    public OfferBookSide(BookSide _side) {
        side =_side;
        bookHalf = new Vector<BookRow>();
    }



    ////////////////////////////////////////////////////////////////////

    private BookSide side;
    private Vector<BookRow> bookHalf;

}
