package org.yats.trading;

import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class OrderNew extends OrderBase {

    public static OrderNew NULL = new OrderNewNull();


    public OrderCancel createCancelOrder()
    {
        return OrderCancel.create()
                .withProduct(product)
                .withBookSide(bookSide)
                .withExternalAccount(externalAccount)
                .withOrderId(orderId)
                ;
    }


    @Override
    public String toString() {
        return "OrderNew{" +
                "oid="+getOrderId()+
                ","+product +
                ",bookSide=" + bookSide +
                ",limit=" + limit +
                ",size=" + size +
                ",externalAccount=" + externalAccount +
                ",internalAccount=" + internalAccount +
                ",timestamp=" + timestamp +
                '}';
    }

    public Receipt createReceiptDefault() {
        Receipt r = Receipt.create()
                .withOrderId(getOrderId())
                .withExternalAccount(externalAccount)
                .withProduct(product)
                .withBookSide(bookSide)
                .withPrice(limit)
                .withResidualSize(size)
                .withTotalTradedSize(0)
                .withCurrentTradedSize(0)
                .withRejectReason("")
                ;
        return r;
    }

    public Product getProduct() {
        return product;
    }

//    public void setProduct(Product product) {
//        this.product = product;
//    }

    public BookSide getBookSide() {
        return bookSide;
    }

    public double getSize() {
        return size;
    }

    public double getLimit() {
        return limit;
    }


    public String getExternalAccount() {
        return externalAccount;
    }


    public OrderNew withTimestamp(DateTime d) {
        timestamp=d;
        return this;
    }

    public OrderNew withOrderId(UniqueId i) {
        setOrderId(i);
        return this;
    }

    public OrderNew withExternalAccount(String a) {
        externalAccount =a;
        return this;
    }

    public OrderNew withInternalAccount(String a) {
        internalAccount=a;
        return this;
    }

    public OrderNew withProduct(Product p) {
        product=p;
        return this;
    }

    public OrderNew withBookSide(BookSide b) {
        bookSide=b;
        return this;
    }

    public OrderNew withSize(double s) {
        size=s;
        return this;
    }

    public OrderNew withLimit(double l) {
        limit=l;
        return this;
    }

    public static OrderNew create() {
        return new OrderNew();
    }

    public OrderNew() {
        product = Product.NULL;
        bookSide = BookSide.BID;
        externalAccount ="";
        setOrderId(UniqueId.create());
        timestamp = DateTime.now(DateTimeZone.UTC);
        internalAccount="";
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    private DateTime timestamp;
    private String externalAccount;
    private String internalAccount;
    private Product product;
    private BookSide bookSide;
    private double size;
    private double limit;

    private static class OrderNewNull extends OrderNew {

        private OrderNewNull() {
            super();
        }

        @Override
        public String toString() {
            return "This is OrderNewNULL";
        }

        public Product getProduct() {
            throw new RuntimeException("This is OrderNewNULL");

        }
    }
} // class
