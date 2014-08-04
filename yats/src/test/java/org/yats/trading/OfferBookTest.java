package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class OfferBookTest {

    @Test
    public void canConvertToAndFromCSV() {
        String csv = book.toStringCSV();
        OfferBook csvBook = OfferBook.fromStringCSV(csv);
        String csv2 = csvBook.toStringCSV();
        assert(csv.compareTo(csv2)==0);
    }

    @Test
    public void canGetSingleRowsFromBook() {
        assert(book.getDepth(BookSide.BID)==2);
        assert(book.getDepth(BookSide.ASK)==0);
        assert(book.getBookRow(BookSide.BID, 0).isPrice(Decimal.fromString("22")));
        assert(book.getBookRow(BookSide.BID, 0).isSize(Decimal.fromString("10")));
        assert(book.getBookRow(BookSide.BID, 1).isPrice(Decimal.fromString("23")));
        assert(book.getBookRow(BookSide.BID, 1).isSize(Decimal.fromString("11")));
    }

    @BeforeMethod
    public void setUp() {
        book = new OfferBook();
        book.addBid("10", "22");
        book.addBid("11", "23");
    }

    private OfferBook book;

} // class
