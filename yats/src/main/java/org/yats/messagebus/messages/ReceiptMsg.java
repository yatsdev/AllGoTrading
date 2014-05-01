package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.Product;
import org.yats.trading.Receipt;

public class ReceiptMsg {


    public String getTopic() {
        return MarketDataMsg.class.getSimpleName()+"."+orderId;
    }

    public static ReceiptMsg createFromReceipt(Receipt r)
    {
        ReceiptMsg m = new ReceiptMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.externalAccount=r.getExternalAccount();
        m.internalAccount=r.getExternalAccount();
        m.productId = r.getProduct().getId();
        m.symbol = r.getProduct().getSymbol();
        m.exchange = r.getProduct().getExchange();
        m.bookSideDirection = r.getBookSide().toDirection();
        m.residualSize=r.getResidualSize();
        m.currentTradedSize=r.getCurrentTradedSize();
        m.totalTradedSize = r.getTotalTradedSize();
        m.price=r.getPrice();
        m.rejectReason=r.getRejectReason();
        m.endState=r.isEndState();
        return m;
    }

    public Receipt toReceipt() {
        Receipt r = Receipt.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withExternalAccount(externalAccount)
                .withInternalAccount(internalAccount)
                .withProduct(new Product(productId, symbol,exchange))
                .withBookSide(BookSide.fromDirection(bookSideDirection))
                .withResidualSize(residualSize)
                .withCurrentTradedSize(currentTradedSize)
                .withTotalTradedSize(totalTradedSize)
                .withPrice(price)
                .withRejectReason(rejectReason)
                .withEndState(endState)
                ;
        return r;
    }


    @Override
    public String toString() {
        return "ReceiptMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
                ", externalAccount='" + externalAccount + '\'' +
                ", internalAccount='" + internalAccount + '\'' +
                ", productId='" + productId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", bookSideDirection=" + bookSideDirection +
                ", residualSize=" + residualSize +
                ", currentTradedSize=" + currentTradedSize +
                ", totalTradedSize=" + totalTradedSize +
                ", price=" + price +
                ", rejectReason='" + rejectReason + '\'' +
                ", endState=" + endState +
                '}';
    }

    public ReceiptMsg() {
    }

    public String timestamp;
    public String orderId;
    public String externalAccount;
    public String internalAccount;
    public String productId;
    public String symbol;
    public String exchange;
    public int bookSideDirection;
    public double residualSize;
    public double currentTradedSize;
    public double totalTradedSize;
    public double price;
    public String rejectReason;
    public boolean endState;

} // class
