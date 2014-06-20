package org.yats.connectivity.matching;

import org.yats.common.Decimal;
import org.yats.trading.OrderNew;

import java.util.SortedMap;
import java.util.TreeMap;

public class LimitOrderBook {

    private int size;

    public LimitOrderBook() {
        book = new TreeMap<Decimal, OrderList>();
    }

    private SortedMap<Decimal,OrderList> book;

    public void add(OrderNew orderNew) {

    }

    public int getSize() {
        return size;
    }
} // class
