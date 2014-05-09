package org.yats.trading;

import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class OrderCancel extends OrderBase {

    @Override
    public String toString() {
        return "OrderCancel{" +
                "oid="+getOrderId()+
                ",pid="+ productId +
                ",bookSide=" + bookSide +
                ",externalAccount=" + externalAccount +
                ",timestamp=" + timestamp +
                '}';
    }

    public Receipt createReceiptDefault() {
        return Receipt.create()
                .withOrderId(orderId)
                .withProductId(productId)
                .withBookSide(bookSide)
                .withExternalAccount(externalAccount)
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

    public String getExternalAccount() {
        return externalAccount;
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

    public OrderCancel withExternalAccount(String a) {
        externalAccount =a;
        return this;
    }

    public static OrderCancel create() {
        return new OrderCancel();
    }

    public OrderCancel() {
        productId = "";
        bookSide = BookSide.BID;
        externalAccount ="";
        orderId = UniqueId.create();
        timestamp = DateTime.now(DateTimeZone.UTC);
    }

    private DateTime timestamp;
    private String productId;
    private BookSide bookSide;
    private String externalAccount;

} // class
