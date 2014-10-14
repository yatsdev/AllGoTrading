package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;

/**
 * Created by macbook52 on 14/10/14.
 */
public class StorePriceCSVTest {
    private static String filename = "StorePriceCSVTest.csv";
    final Logger log = LoggerFactory.getLogger(StorePriceCSVTest.class);

    @Test
    public void canWriteAndReadLastCSV() {
        FileTool.deleteFile(filename);
        StorePriceCSV storage = new StorePriceCSV(filename);
        PriceData p1 = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.ONE);
        storage.store(p1);
        PriceData p2 = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.TWO);
        storage.store(p2);
        PriceData newPrice = storage.readLast();
        log.info("priceData " + newPrice.toString());
        log.info("p2 " + p2.toString());
        assert (newPrice.isSameFrontRowPricesAs(p2));
        FileTool.deleteFile(filename);
    }

    @BeforeMethod
    public void setUp() {

    }

}
