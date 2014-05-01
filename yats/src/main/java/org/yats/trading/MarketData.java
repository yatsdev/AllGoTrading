package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

public class MarketData
{
    public static MarketDataNULL NULL = new MarketDataNULL();



    public boolean isPriceAndSizeSame(MarketData other) {
        if(other==NULL) return false;
        if(bid!=other.bid) return false;
        if(ask!=other.ask) return false;
        if(bidSize!=other.bidSize) return false;
        if(askSize!=other.askSize) return false;
        return true;
    }

    public boolean isSameAs(MarketData other) {
        if(other==NULL) return false;
        if(bid!=other.bid) return false;
        if(ask!=other.ask) return false;
        if(bidSize!=other.bidSize) return false;
        if(askSize!=other.askSize) return false;
        if(securityId.compareTo(other.securityId)!=0) return false;
        if(timestamp.toString().compareTo(other.timestamp.toString())!=0) return false;
        return true;
    }

    public boolean isInitialized() {
        return true;
    }

    public MarketData(DateTime timestamp, String securityId, double bid, double ask, double bidSize, double askSize) {
        this.timestamp = timestamp;
        this.securityId = securityId;
        this.bid = bid;
        this.ask = ask;
        this.bidSize = bidSize;
        this.askSize = askSize;
    }

    @Override
    public String toString() {
        String timeString = timestamp.toString(ISODateTimeFormat.basicDateTime());
        return securityId+", "+bidSize+"@"+bid+" | "+askSize+"@"+ask+"  "+timeString;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getBidSize() {
        return bidSize;
    }

    public void setBidSize(double bidSize) {
        this.bidSize = bidSize;
    }

    public double getAskSize() {
        return askSize;
    }

    public void setAskSize(double askSize) {
        this.askSize = askSize;
    }



    DateTime timestamp;
    String securityId;
    double bid;
    double ask;
    double bidSize;
    double askSize;


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
        public String getSecurityId() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public double getBid() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public double getAsk() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public double getBidSize() {
            throw new RuntimeException("This is NULL!");
        }

        @Override
        public double getAskSize() {
            throw new RuntimeException("This is NULL!");
        }

        private MarketDataNULL() {
            super(DateTime.now(DateTimeZone.UTC),"",0,0,0,0);
        }

    }

} // class
