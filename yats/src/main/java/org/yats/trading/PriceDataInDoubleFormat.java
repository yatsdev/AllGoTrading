package org.yats.trading;


import org.joda.time.DateTime;

public class PriceDataInDoubleFormat {

    private DateTime timestamp;
    private String productId;
    private double bid;
    private double ask;
    private double last;
    private double bidSize;
    private double askSize;
    private double lastSize;
    private OfferBook book;


    public PriceDataInDoubleFormat(PriceData priceData){
        this.timestamp=priceData.getTimestamp();
        this.productId=priceData.getProductId();
        this.bid=priceData.getBid().toDouble();
        this.ask=priceData.getAsk().toDouble();
        this.last=priceData.getLast().toDouble();
        this.bidSize=priceData.getBidSize().toDouble();
        this.askSize=priceData.getAskSize().toDouble();
        this.lastSize=priceData.getLastSize().toDouble();
        this.book=priceData.getBook();
    }


    public String getProductId() {
        return productId;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getLast() {
        return last;
    }

    public double getAskSize() {
        return askSize;
    }

    public double getBidSize() {
        return bidSize;
    }

    public double getLastSize() {
        return lastSize;
    }

    public OfferBook getBook() {
        return book;
    }
}
