package org.yats.trading;

import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class OrderCancel extends OrderBase {


    @Override
    public String toString() {
        return "OrderCancel{" +
                "oid="+getOrderId()+
                ","+product +
                ",bookSide=" + bookSide +
                ",externalAccount=" + externalAccount +
                ",timestamp=" + timestamp +
                '}';
    }

    public Receipt createReceiptDefault() {
        Receipt r = Receipt.create()
                .withOrderId(orderId)
                .withProduct(product)
                .withBookSide(bookSide)
                .withExternalAccount(externalAccount)
                .withEndState(false)
                ;
        return r;
    }

    public BookSide getSide() {
        return bookSide;
    }

    public Product getProduct() {
        return product;
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

    public OrderCancel withProduct(Product p) {
        product=p;
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
        product = Product.NULL;
        bookSide = BookSide.BID;
        externalAccount ="";
        orderId = UniqueId.create();
        timestamp = DateTime.now(DateTimeZone.UTC);
    }

    DateTime timestamp;
    Product product;
    BookSide bookSide;
    String externalAccount;

} // class
