package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.trading.OrderNew;

public class LimitOrderBookTest {

    @Test
    public void canConvertPositionRequest()
    {
        book.add(new OrderNew());
        assert (2 == book.getSize());
    }


    @BeforeMethod
    public void setUp() {
        book = new LimitOrderBook();
    }

    LimitOrderBook book;
} // class
