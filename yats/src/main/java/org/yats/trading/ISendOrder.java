package org.yats.trading;

public interface ISendOrder {
    void sendOrderNew(OrderNew order);
    void sendOrderCancel(OrderCancel orderCancel);
}
