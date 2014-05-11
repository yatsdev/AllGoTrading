package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.math.BigDecimal;

public class MarketData
{
    public static MarketDataNULL NULL = new MarketDataNULL();


    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    public boolean isPriceAndSizeSame(MarketData other) {
        if(other==NULL) return false;
        if(bid.compareTo(other.bid)!=0) return false;
        if(ask.compareTo(other.ask)!=0) return false;
        if(bidSize.compareTo(other.bidSize)!=0) return false;
        return askSize == other.askSize;
    }

    public boolean isSameAs(MarketData other) {
        if(other==NULL) return false;
        if(bid.compareTo(other.bid)!=0) return false;
        if(ask.compareTo(other.ask)!=0) return false;
        if(bidSize.compareTo(other.bidSize)!=0) return false;
        if(askSize.compareTo(other.askSize)!=0) return false;
        if(productId.compareTo(other.productId)!=0) return false;
        if(timestamp.toString().compareTo(other.timestamp.toString())!=0) return false;
        return true;
    }

    public boolean isInitialized() {
        return true;
    }

    public MarketData(DateTime timestamp, String productId, BigDecimal bid, BigDecimal ask, BigDecimal bidSize, BigDecimal askSize) {
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

    public BigDecimal getBid() {
        return bid;
    }

//    public void setBid(BigDecimal bid) {
//        this.bid = bid;
//    }

    public BigDecimal getAsk() {
        return ask;
    }

//    public void setAsk(BigDecimal ask) {
//        this.ask = ask;
//    }

    public BigDecimal getBidSize() {
        return bidSize;
    }

//    public void setBidSize(BigDecimal bidSize) {
//        this.bidSize = bidSize;
//    }

    public BigDecimal getAskSize() {
        return askSize;
    }

//    public void setAskSize(BigDecimal askSize) {
//        this.askSize = askSize;
//    }



    DateTime timestamp;
    String productId;
    BigDecimal bid;
    BigDecimal ask;
    BigDecimal bidSize;
    BigDecimal askSize;



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
        public BigDecimal getBid() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public BigDecimal getAsk() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public BigDecimal getBidSize() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public BigDecimal getAskSize() {
            throw new RuntimeException("This is NULL!");
        }

        private MarketDataNULL() {
            super(DateTime.now(DateTimeZone.UTC),"", BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
        }

    }

} // class
