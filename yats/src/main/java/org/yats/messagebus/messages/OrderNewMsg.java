package org.yats.messagebus.messages;

import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.OrderNew;

public class OrderNewMsg {

    public String getTopic() {
        return ""+orderId;
    }


    public static OrderNewMsg createFromOrderNew(OrderNew r)
    {
        OrderNewMsg m = new OrderNewMsg();
        m.timestamp = r.getTimestamp().toString();
        m.orderId=r.getOrderId().toString();
        m.internalAccount=r.getInternalAccount();
        m.productId = r.getProductId();
        m.bookSideDirection = r.getBookSide().toDirection();
        m.size=r.getSize().toString();
        m.limit=r.getLimit().toString();
        return m;
    }

    public OrderNew toOrderNew() {
        return OrderNew.create()
                .withTimestamp(DateTime.parse(timestamp))
                .withOrderId(UniqueId.createFromString(orderId))
                .withInternalAccount(internalAccount)
                .withProductId(productId)
                .withBookSide(BookSide.fromDirection(bookSideDirection))
                .withSize(new Decimal(size))
                .withLimit(new Decimal(limit));
    }

    @Override
    public String toString() {
        return "OrderNewMsg{" +
                "timestamp='" + timestamp + '\'' +
                ", orderId='" + orderId + '\'' +
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

  public boolean isSameAs(OrderNewMsg data) {

        if(timestamp.compareTo(data.timestamp)!=0) return false;
        if(orderId.compareTo(data.orderId)!=0) return false;
        if(internalAccount.compareTo(data.internalAccount)!=0) return false;
        if(productId.compareTo(data.productId)!=0) return false;
        if(symbol.compareTo(data.symbol)!=0) return false;
        if(exchange.compareTo(data.exchange)!=0) return false;
        if(!(bookSideDirection==data.bookSideDirection))return false;
        if(size.compareTo(data.size)!=0) return false;
      return limit.compareTo(data.limit) == 0;

  }

    public String timestamp;
    public String orderId;
    public String internalAccount;
    public String productId;
    public String symbol;
    public String exchange;
    public int bookSideDirection;
    public String size;
    public String limit;

} // class
