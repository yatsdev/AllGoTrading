package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.trading.MarketData;

public class MarketDataMsg {

    public String getTopic() {
        return "" + productId;
    }

    public static MarketDataMsg createFrom(MarketData marketData) {
        MarketDataMsg m = new MarketDataMsg();
        m.bid=marketData.getBid().toString();
        m.ask=marketData.getAsk().toString();
        m.last=marketData.getLast().toString();
        m.bidSize=marketData.getBidSize().toString();
        m.askSize=marketData.getAskSize().toString();
        m.lastSize=marketData.getLastSize().toString();
        m.productId =marketData.getProductId();
        m.timestamp=marketData.getTimestamp().toString();
        return m;
    }

    public MarketData toMarketData()
    {
        return new MarketData(
                DateTime.parse(timestamp),
                productId,
                new Decimal(bid),
                new Decimal(ask),
                new Decimal(last),
                new Decimal(bidSize),
                new Decimal(askSize),
                new Decimal(lastSize));
    }

    public boolean isSameAs(MarketDataMsg data) {
        if(bid.compareTo(data.bid)!=0) return false;
        if(ask.compareTo(data.ask)!=0) return false;
        if(last.compareTo(data.last)!=0) return false;
        if(bidSize.compareTo(data.bidSize)!=0) return false;
        if(askSize.compareTo(data.askSize)!=0) return false;
        if(lastSize.compareTo(data.lastSize)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        if(timestamp.compareTo(data.timestamp)!=0) return false;
        return true;
    }

    @Override
    public String toString() {
        return "MarketDataMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", productId='" + productId + '\'' +
                ", bid='" + bid + '\'' +
                ", ask='" + ask + '\'' +
                ", last='" + last + '\'' +
                ", bidSize='" + bidSize + '\'' +
                ", askSize='" + askSize + '\'' +
                ", lastSize='" + lastSize + '\'' +
                '}';
    }

    public MarketDataMsg() {
    }


    public String timestamp;
    public String productId;
    public String bid;
    public String ask;
    public String last;
    public String bidSize;
    public String askSize;
    public String lastSize;

} // class
