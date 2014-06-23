package org.yats.connectivity.matching;

import org.yats.trading.OrderNew;

import java.util.ArrayList;

public class OrderList {


    public void add(OrderNew order) {
        list.add(order);
    }

    public int size() {
        return list.size();
    }

    public OrderList() {
        list = new ArrayList<OrderNew>();
    }

    ArrayList<OrderNew> list;

} // class
