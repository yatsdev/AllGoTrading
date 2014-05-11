package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;



public class Receipt {

    public static ReceiptNULL NULL = new ReceiptNULL();

    boolean isRejection()
    {
        return rejectReason.length() > 0;
    }

//    public OrderCancel createCancelOrder()
//    {
//        return OrderCancel.create()
//                .withProductId(productId)
//                .withBookSide(bookSide)
//                .withExternalAccount(externalAccount)
//                .withOrderId(orderId)
//                ;
//    }

    public boolean isForOrder(OrderNew order) {
        return orderId.isSameAs(order.getOrderId());
    }

//    public boolean isForSameProductAs(Receipt other)
//    {
//        return other.hasProductId(productId);
//    }

    public boolean isForProduct(Product p) {
        return p.hasProductId(productId);
    }

    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "timestamp=" + timestamp +
                ",orderId=" + orderId +
                ",externalAccount=" + externalAccount +
                ",internalAccount=" + internalAccount +
                ",productId=" + productId +
                ",bookSide=" + bookSide +
                ",residualSize=" + residualSize +
                ",currentTradedSize=" + currentTradedSize +
                ",totalTradedSize=" + totalTradedSize +
                ",price=" + price +
                ",rejectReason=" + rejectReason +
                ",endState=" + endState +
                '}';
    }

    public boolean isForSameOrderAs(Receipt other) {
        return other != NULL && orderId.isSameAs(other.getOrderId());
    }

    public Decimal getPositionChange() {
        return Decimal.createFromDouble(bookSide.toDirection()).multiply(currentTradedSize);
    }


//    public boolean isNewOrModifiedUnfilledOrderInMarket() {
//        return !endState && (residualSize>0) && (totalTradedSize==0);
//    }

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public UniqueId getOrderId() {
        return orderId;
    }

    public void setOrderId(UniqueId orderId) {
        this.orderId = orderId;
    }

    public String getExternalAccount() {
        return externalAccount;
    }

    public void setExternalAccount(String externalAccount) {
        this.externalAccount = externalAccount;
    }

    public BookSide getBookSide() {
        return bookSide;
    }

    public void setBookSide(BookSide bookSide) {
        this.bookSide = bookSide;
    }

    public Decimal getResidualSize() {
        return residualSize;
    }

    public void setResidualSize(Decimal residualSize) {
        this.residualSize = residualSize;
    }

    public Decimal getCurrentTradedSize() {
        return currentTradedSize;
    }

    public void setCurrentTradedSize(Decimal currentTradedSize) {
        this.currentTradedSize = currentTradedSize;
    }

    public Decimal getPrice() {
        return price;
    }

    public void setPrice(Decimal price) {
        this.price = price;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Decimal getTotalTradedSize() {
        return totalTradedSize;
    }

    public void setTotalTradedSize(Decimal totalTradedSize) {
        this.totalTradedSize = totalTradedSize;
    }


    public Receipt withTimestamp(DateTime d) {
        timestamp=d;
        return this;
    }

    public Receipt withOrderId(UniqueId id) {
        orderId = id;
        return this;
    }

    public Receipt withExternalAccount(String a) {
        externalAccount = a;
        return this;
    }

    public Receipt withInternalAccount(String ia) {
        internalAccount = ia;
        return this;
    }

    public Receipt withProductId(String pid) {
        productId = pid;
        return this;
    }

    public Receipt withBookSide(BookSide b) {
        bookSide=b;
        return this;
    }

    public Receipt withResidualSize(Decimal d) {
        residualSize=d;
        return this;
    }

    public Receipt withCurrentTradedSize(Decimal d) {
        currentTradedSize=d;
        return this;
    }

    public Receipt withTotalTradedSize(Decimal d) {
        totalTradedSize=d;
        return this;
    }

    public Receipt withPrice(Decimal p) {
        price=p;
        return this;
    }

    public Receipt withRejectReason(String r) {
        rejectReason=r;
        return this;
    }

    public Receipt withEndState(boolean e) {
        endState=e;
        return this;
    }

    public void setInternalAccount(String internalAccount) {
        this.internalAccount = internalAccount;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public static Receipt create() {
        return new Receipt();
    }

    public Receipt() {
        endState=false;
        productId = "";
        bookSide = BookSide.NULL;
        timestamp = DateTime.now(DateTimeZone.UTC);
        orderId = UniqueId.create();
        currentTradedSize=Decimal.ZERO;
        residualSize=Decimal.ZERO;
        totalTradedSize=Decimal.ZERO;
        rejectReason="";
        price=Decimal.ZERO;
        externalAccount="";
        internalAccount="";
    }

    private DateTime timestamp;
    private UniqueId orderId;
    private String externalAccount;
    private String internalAccount;
    private String productId;
    private BookSide bookSide;
    private Decimal residualSize;
    private Decimal currentTradedSize;
    private Decimal totalTradedSize;
    private Decimal price;
    private String rejectReason;
    private boolean endState;

    private static class ReceiptNULL extends Receipt {
        private ReceiptNULL() {
        }

        @Override
        public UniqueId getOrderId() {
            throw new RuntimeException("This is NULL");
        }
    }
} // class
