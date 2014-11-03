package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.OrderCancel;

public class OrderCancelMsg {

    public String getTopic() {
        return OrderCancelMsg.class.getSimpleName()+"."+orderId;
    }


    public static OrderCancelMsg createFromOrderCancel(OrderCancel r)
    {
        OrderCancelMsg m = new OrderCancelMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.productId = r.getProductId();
        m.bookSideDirection = r.getBookSide().toDirection();
        return m;
    }

    public OrderCancel toOrderCancel() {
        return OrderCancel.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withProductId(productId)
                .withBookSide(BookSide.fromDirection(bookSideDirection));
    }

    @Override
    public String toString() {
        return "OrderCancelMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", bookSideDirection=" + bookSideDirection +
                '}';
    }

    public OrderCancelMsg() {
    }

    public boolean isSameAs(OrderCancelMsg data) {

        if(timestamp.compareTo(data.timestamp)!=0) return false;
        if(orderId.compareTo(data.orderId)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        return bookSideDirection == (data.bookSideDirection);
    }

    public String timestamp;
    public String orderId;
    public String productId;
    public int bookSideDirection;

} // class
