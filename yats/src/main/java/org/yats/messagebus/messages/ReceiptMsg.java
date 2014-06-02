package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.Receipt;

public class ReceiptMsg {


    public String getTopic() {
        return ""+orderId;
    }

    public static ReceiptMsg fromReceipt(Receipt r)
    {
        ReceiptMsg m = new ReceiptMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.externalAccount=r.getExternalAccount();
        m.internalAccount=r.getExternalAccount();
        m.productId = r.getProductId();
        m.bookSideDirection = r.getBookSide().toDirection();
        m.residualSize=r.getResidualSize().toString();
        m.currentTradedSize=r.getCurrentTradedSize().toString();
        m.totalTradedSize = r.getTotalTradedSize().toString();
        m.price=r.getPrice().toString();
        m.rejectReason=r.getRejectReason();
        m.endState=r.isEndState();
        return m;
    }

    public Receipt toReceipt() {
        return Receipt.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withExternalAccount(externalAccount)
                .withInternalAccount(internalAccount)
                .withProductId(productId)
                .withBookSide(BookSide.fromDirection(bookSideDirection))
                .withResidualSize(new Decimal(residualSize))
                .withCurrentTradedSize(new Decimal(currentTradedSize))
                .withTotalTradedSize(new Decimal(totalTradedSize))
                .withPrice(new Decimal(price))
                .withRejectReason(rejectReason)
                .withEndState(endState);
    }


    @Override
    public String toString() {
        return "ReceiptMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
                ", externalAccount='" + externalAccount + '\'' +
                ", internalAccount='" + internalAccount + '\'' +
                ", productId='" + productId + '\'' +
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
    public int bookSideDirection;
    public String residualSize;
    public String currentTradedSize;
    public String totalTradedSize;
    public String price;
    public String rejectReason;
    public boolean endState;

} // class
