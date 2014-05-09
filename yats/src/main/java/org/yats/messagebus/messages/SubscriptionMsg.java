package org.yats.messagebus.messages;

public class SubscriptionMsg {

    @Override
    public String toString() {
        return "SubscriptionMsg{" +
                "productId=" + productId +
                '}';
    }

    public static SubscriptionMsg createFromProductId(String pid)
    {
        SubscriptionMsg m = new SubscriptionMsg();
        m.productId = pid;
        return m;
    }

    public SubscriptionMsg() {
    }


    public String productId;
} // class
