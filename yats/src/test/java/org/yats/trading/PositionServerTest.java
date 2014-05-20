package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PositionServerTest {

    @Test
    public void canProvidePosition()
    {

    }

    @BeforeMethod
    public void setUp() {
        positionServer = new PositionServer();
    }

    private PositionServer positionServer;
}
