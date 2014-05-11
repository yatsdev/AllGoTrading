package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.OrderNew;

public class OrderNewMsg {

    public String getTopic() {
        return MarketDataMsg.class.getSimpleName()+"."+orderId;
    }


    public static OrderNewMsg createFromOrderNew(OrderNew r)
    {
        OrderNewMsg m = new OrderNewMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.externalAccount=r.getExternalAccount();
        m.internalAccount=r.getExternalAccount();
        m.productId = r.getProductId();
        m.bookSideDirection = r.getBookSide().toDirection();
        m.size=r.getSize();
        m.limit=r.getLimit();
        return m;
    }

    public OrderNew toOrderNew() {
        return OrderNew.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withExternalAccount(externalAccount)
                .withInternalAccount(internalAccount)
                .withProductId(productId)
                .withBookSide(BookSide.fromDirection(bookSideDirection))
                .withSize(size)
                .withLimit(limit);
    }

    @Override
    public String toString() {
        return "OrderNewMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
                ", externalAccount='" + externalAccount + '\'' +
                ", internalAccount='" + internalAccount + '\'' +
                ", productId='" + productId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", bookSideDirection=" + bookSideDirection +
                ", size=" + size +
                ", limit=" + limit +
                '}';
    }

    public OrderNewMsg() {
    }

    public String timestamp;
    public String orderId;
    public String externalAccount;
    public String internalAccount;
    public String productId;
    public String symbol;
    public String exchange;
    public int bookSideDirection;
    public java.math.BigDecimal size;
    public java.math.BigDecimal limit;

} // class
