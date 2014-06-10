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

    public boolean isPriceAndSizeSame(MarketData other) {
        if(other==NULL) return false;
        if(!bid.isEqualTo(other.bid)) return false;
        if(bidSize.isEqualTo(other.bidSize)) return false;
        if(!last.isEqualTo(other.last)) return false;
        if(lastSize.isEqualTo(other.lastSize)) return false;
        if(!ask.isEqualTo(other.ask)) return false;
        return askSize == other.askSize;
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
        if(timestamp.toString().compareTo(other.timestamp.toString())!=0) return false;
        return true;
    }

    public boolean isInitialized() {
        return true;
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

    private DateTime timestamp;
    private String productId;
    private Decimal bid;
    private Decimal ask;
    private Decimal last;
    private Decimal bidSize;
    private Decimal askSize;
    private Decimal lastSize;

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
