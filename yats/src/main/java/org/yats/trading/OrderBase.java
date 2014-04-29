package org.yats.trading;

import org.yats.common.UniqueId;

public abstract class OrderBase {


    public boolean isSameOrderId(OrderBase other) {
        return orderId.isSameAs(other.orderId);
    }

    public UniqueId getOrderId() {
        return orderId;
    }

    public void setOrderId(UniqueId orderId) {
        this.orderId = orderId;
    }

    protected UniqueId orderId;

} // class
