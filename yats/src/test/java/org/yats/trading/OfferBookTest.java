package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OfferBookTest {

    @Test
    public void canConvertToAndFromCSV() {
        String csv = book.toStringCSV();
        OfferBook csvBook = OfferBook.fromStringCSV(csv);
        String csv2 = csvBook.toStringCSV();
        assert(csv.compareTo(csv2)==0);
    }

    @BeforeMethod
    public void setUp() {
        book = new OfferBook();
        book.addBid("10", "22");
        book.addBid("11", "23");
    }

    private OfferBook book;

} // class
