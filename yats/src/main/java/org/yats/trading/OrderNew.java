package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;



public class OrderNew extends OrderBase {

    public static OrderNew NULL = new OrderNewNull();


    public boolean isExecutingWith(Decimal frontRowPrice) {
        if(bookSide.isMoreBehindThan(limit, frontRowPrice)) return false;
        return true;
    }


    public OrderCancel createCancelOrder()
    {
        return OrderCancel.create()
                .withProductId(productId)
                .withBookSide(bookSide)
                .withOrderId(orderId)
                ;
    }


    @Override
    public String toString() {
        return "OrderNew{" +
                "oid="+getOrderId()+
                ",productId="+ productId +
                ",bookSide=" + bookSide +
                ",limit=" + limit +
                ",size=" + size +
                ",internalAccount=" + internalAccount +
                ",timestamp=" + timestamp +
                '}';
    }

    public Receipt createReceiptDefault() {
        return Receipt.create()
                .withInternalAccount(internalAccount)
                .withOrderId(getOrderId())
                .withProductId(productId)
                .withBookSide(bookSide)
                .withPrice(limit)
                .withResidualSize(size)
                .withTotalTradedSize(Decimal.ZERO)
                .withCurrentTradedSize(Decimal.ZERO)
                .withRejectReason("");
    }

    public boolean isForBookSide(BookSide _side) {
        return bookSide.toIndex() == _side.toIndex();
    }

    public BookSide getBookSide() {
        return bookSide;
    }

    public Decimal getSize() {
        return size;
    }

    public Decimal getLimit() {
        return limit;
    }

    public OrderNew withTimestamp(DateTime d) {
        timestamp=d;
        return this;
    }

    public OrderNew withOrderId(UniqueId i) {
        setOrderId(i);
        return this;
    }

    public OrderNew withInternalAccount(String a) {
        internalAccount=a;
        return this;
    }

    public OrderNew withProductId(String pid) {
        productId =pid;
        return this;
    }

    public OrderNew withBookSide(BookSide b) {
        bookSide=b;
        return this;
    }

    public OrderNew withSize(Decimal s) {
        size=s;
        return this;
    }

    public OrderNew withLimit(Decimal l) {
        limit=l;
        return this;
    }

    public static OrderNew create() {
        return new OrderNew();
    }


    public DateTime getTimestamp() {
        return timestamp;
    }

//    public void setTimestamp(DateTime timestamp) {
//        this.timestamp = timestamp;
//    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public OrderNew() {
        bookSide = BookSide.BID;
        setOrderId(UniqueId.create());
        timestamp = DateTime.now(DateTimeZone.UTC);
        internalAccount="";
        size = Decimal.ZERO;
        limit = Decimal.ZERO;
    }


    public boolean isSameAs(OrderNew data) {

        if(timestamp.compareTo(data.timestamp)!=0) return false;
        if(internalAccount.compareTo(data.internalAccount)!=0) return false;
        if(!(bookSide.toDirection()==(data.bookSide.toDirection()))) return false;
        if(!(size.isEqualTo(data.size))) return false;
        if(!(limit.isEqualTo(data.limit))) return false;
        if(productId.compareTo(data.productId)!=0) return false;

        return true;
    }


    private DateTime timestamp;
    private String internalAccount;
//    private Product product;
    private BookSide bookSide;
    private Decimal size;
    private Decimal limit;
    private String productId;



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
