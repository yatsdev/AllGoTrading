package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.trading.MarketData;

public class MarketDataMsg {

    public String getTopic() {
        return MarketDataMsg.class.getSimpleName()+"."+ productId;
    }

    public static MarketDataMsg createFrom(MarketData marketData) {
        MarketDataMsg m = new MarketDataMsg();
        m.bid=marketData.getBid();
        m.ask=marketData.getAsk();
        m.bidSize=marketData.getBidSize();
        m.askSize=marketData.getAskSize();
        m.productId =marketData.getProductId();
        m.timestamp=marketData.getTimestamp().toString();
        return m;
    }

    public MarketData toMarketData()
    {
        return new MarketData(DateTime.parse(timestamp), productId, bid, ask, bidSize, askSize);
    }

    public boolean isSameAs(MarketDataMsg data) {
        if(bid.compareTo(data.bid)!=0) return false;
        if(ask.compareTo(data.ask)!=0) return false;
        if(bidSize.compareTo(data.bidSize)!=0) return false;
        if(askSize.compareTo(data.askSize)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        if(timestamp.compareTo(data.timestamp)!=0) return false;
        return true;
    }

    @Override
    public String toString() {
        return "MarketDataMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", productId='" + productId + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", bidSize=" + bidSize +
                ", askSize=" + askSize +
                '}';
    }

    public MarketDataMsg() {
    }


    public String timestamp;
    public String productId;
    public java.math.BigDecimal bid;
    public java.math.BigDecimal ask;
    public java.math.BigDecimal bidSize;
    public java.math.BigDecimal askSize;

} // class
