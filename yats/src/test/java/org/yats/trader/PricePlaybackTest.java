package org.yats.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;
import org.yats.trading.PriceData;
import org.yats.trading.ProductTest;
import org.yats.trading.StorePriceCSV;

/**
 * Created by abbanerjee on 18/10/14.
 */
public class PricePlaybackTest {

    private static String TEST_PRODUCT1_ID = "TEST_PRODUCT1_ID";
    private static String TEST_PRODUCT2_ID = "TEST_PRODUCT2_ID";
    private static String baseLocation = "data";
    final Logger log = LoggerFactory.getLogger(PricePlaybackTest.class);

    @Test
    public void canMaintainTimeOrder() {
        FileTool.deleteFile(TEST_PRODUCT1_ID);
        FileTool.deleteFile(TEST_PRODUCT2_ID);
        StorePriceCSV p1_storage = new StorePriceCSV(baseLocation,TEST_PRODUCT1_ID);
        PriceData p1 = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.ONE);
        p1_storage.store(p1);
        try{
            Thread.sleep(500);
        }
        catch (InterruptedException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }

        StorePriceCSV p2_storage = new StorePriceCSV(baseLocation,TEST_PRODUCT1_ID);
        PriceData p2 = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.TWO);
        p2_storage.store(p2);


    }

    @BeforeMethod
    public void setUp() {

    }
}
