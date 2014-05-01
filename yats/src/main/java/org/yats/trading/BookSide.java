package org.yats.trading;

public abstract class BookSide {

    public static BookSide NULL = new BookSideNULL();
    public static Bid BID = new Bid();
    public static Ask ASK = new Ask();

    public abstract int toDirection();
    public static BookSide fromDirection(int direction) {
        return direction < 0 ? ASK : BID;
    }



    public static class Ask extends BookSide {
        @Override
        public int toDirection() { return -1; }
        @Override
        public String toString() {return "ASK";}
    } // class Ask

    public static class Bid extends BookSide {
        @Override
        public int toDirection() { return 1; }
        @Override
        public String toString() {return "BID";}

    } // class Ask

    private static class BookSideNULL extends BookSide {
        @Override
        public int toDirection() {
            throw new RuntimeException("This is BookSideNULL");
        }
    }
} // class

