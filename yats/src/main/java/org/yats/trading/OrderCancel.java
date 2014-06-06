package org.yats.trading;

import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class OrderCancel extends OrderBase {

    @Override
    public String toString() {
        return "OrderCancel{" +
                "oid="+getOrderId()+
                ",productId="+ productId +
                ",bookSide=" + bookSide +
                ",timestamp=" + timestamp +
                '}';
    }

    public Receipt createReceiptDefault() {
        return Receipt.create()
                .withOrderId(orderId)
                .withProductId(productId)
                .withBookSide(bookSide)
                .withEndState(false);
    }

    public BookSide getSide() {
        return bookSide;
    }

    public String getProductId() {
        return productId;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public BookSide getBookSide() {
        return bookSide;
    }


    public OrderCancel withOrderId(UniqueId id) {
        orderId=id;
        return this;
    }

    public OrderCancel withTimestamp(DateTime d) {
        timestamp=d;
        return this;
    }

    public OrderCancel withProductId(String pid) {
        productId=pid;
        return this;
    }

    public OrderCancel withBookSide(BookSide side) {
        bookSide=side;
        return this;
    }

    public static OrderCancel create() {
        return new OrderCancel();
    }

    public OrderCancel() {
        productId = "";
        bookSide = BookSide.BID;
        orderId = UniqueId.create();
        timestamp = DateTime.now(DateTimeZone.UTC);
    }
    
     public boolean isSameAs(OrderCancel data) {

        if(timestamp.compareTo(data.timestamp)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        if(!(bookSide.toDirection()==(data.bookSide.toDirection()))) return false;
        return true;
    }


    private DateTime timestamp;
    private String productId;
    private BookSide bookSide;

} // class
