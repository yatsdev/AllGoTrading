package org.yats.messagebus.messages;

import org.yats.trading.Product;

public class SubscriptionMsg {

    @Override
    public String toString() {
        return "SubscriptionMsg{" +
                "id=" + id +
                ",symbol=" + symbol +
                ",exchange=" + exchange +
                '}';
    }

    public static SubscriptionMsg createFromProduct(Product product)
    {
        SubscriptionMsg m = new SubscriptionMsg();
        m.id=product.getId();
        m.symbol=product.getSymbol();
        m.exchange=product.getExchange();
        return m;
    }

    public SubscriptionMsg() {
    }


    public String id;
    public String symbol;
    public String exchange;
} // class
