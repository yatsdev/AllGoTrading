package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.OrderCancel;

public class OrderCancelMsg {

    public String getTopic() {
        return ""+orderId;
    }


    public static OrderCancelMsg createFromOrderCancel(OrderCancel r)
    {
        OrderCancelMsg m = new OrderCancelMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.externalAccount=r.getExternalAccount();
        m.productId = r.getProductId();
        m.bookSide = r.getBookSide();
        return m;
    }

    public OrderCancel toOrderCancel() {
        return OrderCancel.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withExternalAccount(externalAccount)
                .withProductId(productId)
                .withBookSide(bookSide);
    }

    @Override
    public String toString() {
        return "OrderCancelMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
                ", externalAccount='" + externalAccount + '\'' +
                ", productId='" + productId + '\'' +
                ", bookSide=" + bookSide +
                '}';
    }

    public OrderCancelMsg() {
    }

    public String timestamp;
    public String orderId;
    public String externalAccount;
    public String productId;
    public BookSide bookSide;

} // class
