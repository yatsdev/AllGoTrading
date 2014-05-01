package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.trading.MarketData;

public class MarketDataMsg {

    public String getTopic() {
        return MarketDataMsg.class.getSimpleName()+"."+securityId;
    }

    public static MarketDataMsg createFrom(MarketData marketData) {
        MarketDataMsg m = new MarketDataMsg();
        m.bid=marketData.getBid();
        m.ask=marketData.getAsk();
        m.bidSize=marketData.getBidSize();
        m.askSize=marketData.getAskSize();
        m.securityId=marketData.getSecurityId();
        m.timestamp=marketData.getTimestamp().toString();
        return m;
    }

    public MarketData toMarketData()
    {
        return new MarketData(DateTime.parse(timestamp), securityId, bid, ask, bidSize, askSize);
    }

    public boolean isSameAs(MarketDataMsg data) {
        if(bid!=data.bid) return false;
        if(ask!=data.ask) return false;
        if(bidSize!=data.bidSize) return false;
        if(askSize!=data.askSize) return false;
        if(securityId.compareTo(data.securityId)!=0) return false;
        if(timestamp.compareTo(data.timestamp)!=0) return false;
        return true;
    }

    @Override
    public String toString() {
        return "MarketDataMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", securityId='" + securityId + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", bidSize=" + bidSize +
                ", askSize=" + askSize +
                '}';
    }

    public MarketDataMsg() {
    }


    public String timestamp;
    public String securityId;
    public double bid;
    public double ask;
    public double bidSize;
    public double askSize;

} // class
