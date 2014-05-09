package org.yats.trading;

import org.yats.common.UniqueId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;

public class Receipt {

    public static ReceiptNULL NULL = new ReceiptNULL();

    boolean isRejection()
    {
        return rejectReason.length() > 0;
    }

    public OrderCancel createCancelOrder()
    {
        return OrderCancel.create()
                .withProduct(product)
                .withBookSide(bookSide)
                .withExternalAccount(externalAccount)
                .withOrderId(orderId)
                ;
    }

    public boolean isForOrder(OrderNew order) {
        return orderId.isSameAs(order.getOrderId());
    }

    public boolean isForSameProductAs(Receipt other)
    {
        return isForProduct(other.getProduct());
    }

    public boolean isForProduct(Product p) {
        return product.isSameAs(p);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "timestamp=" + timestamp +
                ",orderId=" + orderId +
                ",externalAccount=" + externalAccount +
                ",internalAccount=" + internalAccount +
                ",product=" + product +
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
        if(other == NULL) return false;
        return orderId.isSameAs(other.getOrderId());
    }

    public java.math.BigDecimal getPositionChange() {
        return  currentTradedSize.multiply(java.math.BigDecimal.valueOf(bookSide.toDirection()));
    }

    public boolean isNewOrModifiedUnfilledOrderInMarket() {
        return !endState && (residualSize.compareTo(BigDecimal.ZERO)>0) && (totalTradedSize==BigDecimal.ZERO);
    }

    public String getSecurityId()
    {
        return product.getId();
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BookSide getBookSide() {
        return bookSide;
    }

    public void setBookSide(BookSide bookSide) {
        this.bookSide = bookSide;
    }

    public java.math.BigDecimal getResidualSize() {
        return residualSize;
    }

    public void setResidualSize(java.math.BigDecimal residualSize) {
        this.residualSize = residualSize;
    }

    public java.math.BigDecimal getCurrentTradedSize() {
        return currentTradedSize;
    }

    public void setCurrentTradedSize(java.math.BigDecimal currentTradedSize) {
        this.currentTradedSize = currentTradedSize;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    //todo: replace double with fixed decimal type
    public void setPrice(java.math.BigDecimal price) {
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

    public java.math.BigDecimal getTotalTradedSize() {
        return totalTradedSize;
    }

    public void setTotalTradedSize(java.math.BigDecimal totalTradedSize) {
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

    public Receipt withProduct(Product p) {
        product = p;
        return this;
    }

    public Receipt withBookSide(BookSide b) {
        bookSide=b;
        return this;
    }

    public Receipt withResidualSize(java.math.BigDecimal d) {
        residualSize=d;
        return this;
    }

    public Receipt withCurrentTradedSize(java.math.BigDecimal d) {
        currentTradedSize=d;
        return this;
    }

    public Receipt withTotalTradedSize(java.math.BigDecimal d) {
        totalTradedSize=d;
        return this;
    }

    public Receipt withPrice(java.math.BigDecimal p) {
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
        product = Product.NULL;
        bookSide = BookSide.NULL;
        timestamp = DateTime.now(DateTimeZone.UTC);
        orderId = UniqueId.create();
        currentTradedSize=BigDecimal.ZERO;
        residualSize=BigDecimal.ZERO;
        totalTradedSize=BigDecimal.ZERO;
        rejectReason="";
        price=BigDecimal.ZERO;
        externalAccount="";
        internalAccount="";
    }

    private DateTime timestamp;
    private UniqueId orderId;
    private String externalAccount;
    private String internalAccount;
    private Product product;
    private BookSide bookSide;
    private java.math.BigDecimal residualSize;
    private java.math.BigDecimal currentTradedSize;
    private java.math.BigDecimal totalTradedSize;
    private java.math.BigDecimal price;
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
