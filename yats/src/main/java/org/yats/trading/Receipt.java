package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;



public class Receipt {

    public static ReceiptNULL NULL = new ReceiptNULL();

    public boolean isRejection()
    {
        return rejectReason.length() > 0;
    }

    public boolean isTrade()
    {
        return !currentTradedSize.isEqualTo(Decimal.ZERO);
    }


    public AccountPosition getPositionChangeOfBaseAsAccountPosition() {
        return new AccountPosition(getProductId(), getInternalAccount(), getPositionChangeOfBase());
    }

    public AccountPosition getPositionChangeOfCounterAsAccountPosition(IProvideProduct productList) {
        Product p = productList.getProductWith(productId);
        Decimal baseChange = getPositionChangeOfBase();
        Decimal counterChange = baseChange.multiply(price).multiply(p.getContractSize()).negate();
        AccountPosition ap = new AccountPosition(p.getUnitId(), internalAccount, counterChange);
        return ap;
    }

    public boolean isExecutingWith(Decimal frontRowPrice) {
        if(!frontRowPrice.isGreaterThan(Decimal.ZERO)) return false;
        return !bookSide.isMoreBehindThan(price, frontRowPrice);
    }

    public boolean isForOrder(OrderNew order) {
        return orderId.isSameAs(order.getOrderId());
    }

    public boolean isForProduct(Product p) {
        return p.hasProductId(productId);
    }

    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    public void match(Receipt takerReceipt) {
        currentTradedSize = Decimal.min(takerReceipt.getResidualSize(), residualSize);
        takerReceipt.adjustByTradedSize(currentTradedSize);
        adjustByTradedSize(currentTradedSize);
    }

    private void adjustByTradedSize(Decimal _currentTradedSize) {
        currentTradedSize=_currentTradedSize;
        totalTradedSize=totalTradedSize.add(_currentTradedSize);
        residualSize=Decimal.max(Decimal.ZERO, residualSize.subtract(currentTradedSize));
        if(residualSize.isEqualTo(Decimal.ZERO)) endState=true;
    }

    public Receipt createCopy() {
        String csv = toStringCSV();
        return Receipt.fromStringCSV(csv);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "internalAccount=" + internalAccount +
                ",productId=" + productId +
                ",bookSide=" + bookSide +
                ",price=" + price +
                ",currentTradedSize=" + currentTradedSize +
                ",totalTradedSize=" + totalTradedSize +
                ",residualSize=" + residualSize +
                ",orderId=" + orderId +
                ",externalAccount=" + externalAccount +
                ",timestamp=" + timestamp +
                ",rejectReason=" + rejectReason +
                ",endState=" + endState +
                '}';
    }

    public String toStringCSV() {
        return ""
                + timestamp +
                "," + orderId +
                "," + externalAccount +
                "," + internalAccount +
                "," + productId +
                "," + bookSide.toDirection() +
                "," + residualSize +
                "," + currentTradedSize +
                "," + totalTradedSize +
                "," + price +
                "," + rejectReason +
                "," + endState
                ;
    }

    public static Receipt fromStringCSV(String csv) {
        String[] st = csv.split(",");
        return new Receipt()
                .withTimestamp(new DateTime(st[0],DateTimeZone.UTC))
                .withOrderId(UniqueId.createFromString(st[1]))
                .withExternalAccount(st[2])
                .withInternalAccount(st[3])
                .withProductId(st[4])
                .withBookSide(BookSide.fromDirection(Integer.parseInt(st[5])))
                .withResidualSize(new Decimal(st[6]))
                .withCurrentTradedSize(new Decimal(st[7]))
                .withTotalTradedSize(new Decimal(st[8]))
                .withPrice(new Decimal(st[9]))
                .withRejectReason(st[10])
                .withEndState(Boolean.parseBoolean(st[11]))
                ;
    }

    public boolean isForSameOrderAs(Receipt other) {
        return other != NULL && orderId.isSameAs(other.getOrderId());
    }

    public Decimal getPositionChangeOfBase() {
        return Decimal.fromDouble(bookSide.toDirection()).multiply(currentTradedSize);
    }

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
    public String getOrderIdString() {
        return orderId.toString();
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

    public Decimal getCurrentTradedSizeSigned() {

        return bookSide.toSigned(currentTradedSize);
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

    public boolean isBookSide(BookSide side) {
        return bookSide.equals(side);
    }

    public boolean isSamePriceOrInfront(Receipt other) {
        return price.isEqualTo(other.getPrice()) || bookSide.isMoreInfrontThan(price, other.getPrice());
    }

    public boolean isOpposite(BookSide side) {
        return bookSide.isOpposite(side);
    }

    public boolean hasOrderId(String _orderId) {
        return _orderId.compareTo(orderId.toString()) == 0;
    }

    public OrderCancel createOrderCancel(Receipt r) {
        OrderCancel c = new OrderCancel()
                .withBookSide(r.getBookSide())
                .withOrderId(r.getOrderId())
                .withProductId(r.getProductId())
                ;
        return c;
    }


    private static class ReceiptNULL extends Receipt {
        private ReceiptNULL() {
        }

        @Override
        public UniqueId getOrderId() {
            throw new RuntimeException("This is NULL");
        }
    }
} // class
