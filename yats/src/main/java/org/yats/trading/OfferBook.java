package org.yats.trading;

public class OfferBook {

    public static final String CSV_SEPARATOR = "|";

    @Override
    public String toString() {
        return "OfferBook{" +
                "bookBid=" + bookSides[0] +
                ", bookAsk=" + bookSides[1] +
                '}';
    }


    public boolean isBookSideEmpty(BookSide _side) {
        return getDepth(_side)==0;
    }

    public BookRow getRow(BookSide _side, int _row) {
        return bookSides[_side.toIndex()].getRow(_row);
    }

    public int getDepth(BookSide _side) {
        return bookSides[_side.toIndex()].size();
    }

    public BookRow getBookRow(BookSide _side, int index) {
        return bookSides[_side.toIndex()].getRow(index);
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();
        b.append(bookSides[0].toStringCSV());
        b.append(CSV_SEPARATOR);
        b.append((bookSides[1].toStringCSV()));
        return b.toString();
    }

    public static OfferBook fromStringCSV(String csv) {
        OfferBook book = new OfferBook();
        String[] splitArray = csv.split("\\"+CSV_SEPARATOR);
        String[] booksArray = new String[2];
        booksArray[0] = "";
        booksArray[1] = "";
        if(splitArray.length>1) {
            book.bookSides[0] = OfferBookSide.fromStringCSV(splitArray[0], BookSide.BID);
            book.bookSides[1] = OfferBookSide.fromStringCSV(splitArray[1], BookSide.ASK);
        } else if(csv.charAt(0)=='|') {
            book.bookSides[0] = OfferBookSide.fromStringCSV("", BookSide.BID);
            String rightSide = (splitArray.length==0) ? "" : splitArray[0];
            book.bookSides[1] = OfferBookSide.fromStringCSV(rightSide, BookSide.ASK);
        } else {
            book.bookSides[0] = OfferBookSide.fromStringCSV(splitArray[0], BookSide.BID);
            book.bookSides[1] = OfferBookSide.fromStringCSV("", BookSide.ASK);
        }
        return book;
    }

    public void addBid(String size, String price) {
        addBid(new BookRow(size, price));
    }

    public void addBid(BookRow r) {
        bookSides[0].add(r);
    }

    public void addAsk(String size, String price) {
        addAsk(new BookRow(size, price));
    }

    public void addAsk(BookRow r) {
        bookSides[1].add(r);
    }


    public OfferBook(OfferBookSide bidSide, OfferBookSide askSide) {
        bookSides = new OfferBookSide[2];
        bookSides[0] = bidSide;
        bookSides[1] = askSide;
    }

    public OfferBook() {
        bookSides = new OfferBookSide[2];
        bookSides[0] = new OfferBookSide(BookSide.BID);
        bookSides[1] = new OfferBookSide(BookSide.ASK);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private OfferBookSide[] bookSides;

} // class
