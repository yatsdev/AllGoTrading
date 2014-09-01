package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.yats.common.Decimal;


public class MarketData
{
    public static MarketDataNULL NULL = new MarketDataNULL();

    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    public boolean isBookSideEmpty(BookSide _side) {
        return book.isBookSideEmpty(_side);
    }

    public boolean isFrontRowEmpty(BookSide _side) {
        return book.isBookSideEmpty(_side) || book.getBookRow(_side, 0).isSize(Decimal.ZERO);
    }

    public boolean isPriceAndSizeSame(MarketData other) {
        if(other==NULL) return false;
        if(!bid.isEqualTo(other.bid)) return false;
        if(bidSize.isEqualTo(other.bidSize)) return false;
        if(!last.isEqualTo(other.last)) return false;
        if(lastSize.isEqualTo(other.lastSize)) return false;
        return ask.isEqualTo(other.ask) && askSize == other.askSize;
    }

    public boolean isSameAs(MarketData other) {
        if(other==NULL) return false;
        if(!bid.isEqualTo(other.bid)) return false;
        if(!bidSize.isEqualTo(other.bidSize)) return false;
        if(!ask.isEqualTo(other.ask)) return false;
        if(!askSize.isEqualTo(other.askSize)) return false;
        if(!last.isEqualTo(other.last)) return false;
        if(!lastSize.isEqualTo(other.lastSize)) return false;
        if(productId.compareTo(other.productId)!=0) return false;
        return timestamp.toString().compareTo(other.timestamp.toString()) == 0;
    }

    public boolean isSameFrontRowPricesAs(MarketData other) {
        if(other==NULL) return false;
        if(!bid.isEqualTo(other.bid)) return false;
        return ask.isEqualTo(other.ask);
    }

    public boolean isSameFrontRowBidAs(MarketData other) {
        if(other==NULL) return false;
        return bid.isEqualTo(other.bid);
    }

    public boolean isSameFrontRowAskAs(MarketData other) {
        if(other==NULL) return false;
        return ask.isEqualTo(other.ask);
    }

    public boolean isInitialized() {
        return true;
    }


    public boolean hasFrontRow(BookSide _side, BookRow _row) {
        BookRow dataRow = book.getRow(_side, 0);
        return dataRow.isSameAs(_row);
    }



    public BookRow getPrice(BookSide _side, int _row) {
        return book.getBookRow(_side, _row);
    }

    public String getOfferBookAsCSV() {
        return book.toStringCSV();
    }

    public MarketData(DateTime timestamp, String productId, Decimal bid, Decimal ask, Decimal last, Decimal bidSize, Decimal askSize, Decimal lastSize) {
        this.timestamp = timestamp;
        this.productId = productId;
        this.bid = bid;
        this.ask = ask;
        this.last = last;
        this.bidSize = bidSize;
        this.askSize = askSize;
        this.lastSize = lastSize;
        book = new OfferBook();
        book.addBid(new BookRow(bidSize, bid));
        book.addAsk(new BookRow(askSize, ask));
    }

    @Override
    public String toString() {
        String timeString = timestamp.toString(ISODateTimeFormat.basicDateTime());
        return productId +", "+bidSize+"@"+bid+" | "+askSize+"@"+ask+"  "+timeString;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getProductId() {
        return productId;
    }

    public Decimal getBid() {
        return bid;
    }

    public Decimal getAsk() {
        return ask;
    }

    public Decimal getBidSize() {
        return bidSize;
    }

    public Decimal getAskSize() {
        return askSize;
    }

    public Decimal getLast() { return last; }

    public Decimal getLastSize() { return lastSize; }

    public OfferBook getBook() {
        return book;
    }

    public void setBook(OfferBook book) {
        this.book = book;
    }


    public static MarketData createFromLast(String productId, Decimal last) {
        return new MarketData(DateTime.now(DateTimeZone.UTC), productId,
                last.subtract(Decimal.CENT), last.add(Decimal.CENT), last,
                Decimal.ONE, Decimal.ONE, Decimal.ONE);
    }

    public static MarketData createFromLastWithDepth(String productId, Decimal last, int bidDepth, int askDepth, Decimal step) {
        Decimal bidFront=last.subtract(Decimal.CENT);
        Decimal askFront=last.add(Decimal.CENT);
        MarketData m = new MarketData(DateTime.now(DateTimeZone.UTC), productId,
                bidFront, askFront, last,
                Decimal.ONE, Decimal.ONE, Decimal.ONE);

        OfferBookSide bid=new OfferBookSide(BookSide.BID);
        OfferBookSide ask=new OfferBookSide(BookSide.ASK);
        for(int i =0; i<bidDepth; i++) {
            bid.add(new BookRow(Decimal.CENT, bidFront.subtract(step.multiply(Decimal.fromDouble(i)))));
        }
        for(int i =0; i<askDepth; i++) {
            ask.add(new BookRow(Decimal.CENT, askFront.add(step.multiply(Decimal.fromDouble(i)))));
        }
        OfferBook o = new OfferBook(bid,ask);
        m.setBook(o);
        return m;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private DateTime timestamp;
    private String productId;
    private Decimal bid;
    private Decimal ask;
    private Decimal last;
    private Decimal bidSize;
    private Decimal askSize;
    private Decimal lastSize;
    private OfferBook book;



    private static class MarketDataNULL extends MarketData {

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        public DateTime getTimestamp() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public String getProductId() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getBid() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getBidSize() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getAsk() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getAskSize() {
            throw new RuntimeException("This is NULL!");
        }
        @Override
        public Decimal getLast() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getLastSize() {
            throw new RuntimeException("This is NULL!");
        }

        private MarketDataNULL() {
            super(DateTime.now(DateTimeZone.UTC),"", Decimal.ZERO,Decimal.ZERO,Decimal.ZERO,Decimal.ZERO,Decimal.ZERO,Decimal.ZERO);
        }

    }

} // class
