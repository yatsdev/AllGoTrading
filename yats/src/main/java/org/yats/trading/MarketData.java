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
        if(!ask.isEqualTo(other.ask)) return false;
        if(bidSize.isEqualTo(other.bidSize)) return false;
        return askSize == other.askSize;
    }

    public boolean isSameAs(MarketData other) {
        if(other==NULL) return false;
        if(!bid.isEqualTo(other.bid)) return false;
        if(!ask.isEqualTo(other.ask)) return false;
        if(!bidSize.isEqualTo(other.bidSize)) return false;
        if(!askSize.isEqualTo(other.askSize)) return false;
        if(productId.compareTo(other.productId)!=0) return false;
        if(timestamp.toString().compareTo(other.timestamp.toString())!=0) return false;
        return true;
    }

    public boolean isInitialized() {
        return true;
    }

    public MarketData(DateTime timestamp, String productId, Decimal bid, Decimal ask, Decimal bidSize, Decimal askSize) {
        this.timestamp = timestamp;
        this.productId = productId;
        this.bid = bid;
        this.ask = ask;
        this.bidSize = bidSize;
        this.askSize = askSize;
    }

    @Override
    public String toString() {
        String timeString = timestamp.toString(ISODateTimeFormat.basicDateTime());
        return productId +", "+bidSize+"@"+bid+" | "+askSize+"@"+ask+"  "+timeString;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

//    public void setTimestamp(DateTime timestamp) {
//        this.timestamp = timestamp;
//    }

    public String getProductId() {
        return productId;
    }

//    public void setProductId(String productId) {
//        this.productId = productId;
//    }

    public Decimal getBid() {
        return bid;
    }

//    public void setBid(Decimal bid) {
//        this.bid = bid;
//    }

    public Decimal getAsk() {
        return ask;
    }

//    public void setAsk(Decimal ask) {
//        this.ask = ask;
//    }

    public Decimal getBidSize() {
        return bidSize;
    }

//    public void setBidSize(Decimal bidSize) {
//        this.bidSize = bidSize;
//    }

    public Decimal getAskSize() {
        return askSize;
    }

//    public void setAskSize(Decimal askSize) {
//        this.askSize = askSize;
//    }



    DateTime timestamp;
    String productId;
    Decimal bid;
    Decimal ask;
    Decimal bidSize;
    Decimal askSize;



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
        public Decimal getAsk() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getBidSize() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public Decimal getAskSize() {
            throw new RuntimeException("This is NULL!");
        }

        private MarketDataNULL() {
            super(DateTime.now(DateTimeZone.UTC),"", Decimal.ZERO,Decimal.ZERO,Decimal.ZERO,Decimal.ZERO);
        }

    }

} // class
