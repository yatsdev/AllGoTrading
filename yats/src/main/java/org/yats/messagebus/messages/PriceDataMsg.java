package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.trading.PriceData;
import org.yats.trading.OfferBook;

public class PriceDataMsg {

    public String getTopic() {
        return "" + productId;
    }

    public static PriceDataMsg createFrom(PriceData priceData) {
        PriceDataMsg m = new PriceDataMsg();
        m.bid= priceData.getBid().toString();
        m.ask= priceData.getAsk().toString();
        m.last= priceData.getLast().toString();
        m.bidSize= priceData.getBidSize().toString();
        m.askSize= priceData.getAskSize().toString();
        m.lastSize= priceData.getLastSize().toString();
        m.productId = priceData.getProductId();
        m.timestamp= priceData.getTimestamp().toString();
        m.offerBook= priceData.getOfferBookAsCSV();
        return m;
    }

    public PriceData toPriceData()
    {
        PriceData d = new PriceData(
                DateTime.parse(timestamp),
                productId,
                new Decimal(bid),
                new Decimal(ask),
                new Decimal(last),
                new Decimal(bidSize),
                new Decimal(askSize),
                new Decimal(lastSize));
        d.setBook(OfferBook.fromStringCSV(offerBook));
        return d;
    }

    public boolean isSameAs(PriceDataMsg data) {
        if(bid.compareTo(data.bid)!=0) return false;
        if(ask.compareTo(data.ask)!=0) return false;
        if(last.compareTo(data.last)!=0) return false;
        if(bidSize.compareTo(data.bidSize)!=0) return false;
        if(askSize.compareTo(data.askSize)!=0) return false;
        if(lastSize.compareTo(data.lastSize)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        if(timestamp.compareTo(data.timestamp)!=0) return false;
        return offerBook.compareTo(data.offerBook) == 0;
    }

    @Override
    public String toString() {
        return "PriceDataMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", productId='" + productId + '\'' +
                ", bid='" + bid + '\'' +
                ", ask='" + ask + '\'' +
                ", last='" + last + '\'' +
                ", bidSize='" + bidSize + '\'' +
                ", askSize='" + askSize + '\'' +
                ", lastSize='" + lastSize + '\'' +
                ", offerBook='" + offerBook + '\'' +
                '}';
    }

    public PriceDataMsg() {
    }

    public String timestamp;
    public String productId;
    public String bid;
    public String ask;
    public String last;
    public String bidSize;
    public String askSize;
    public String lastSize;
    public String offerBook;

} // class
