package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;

/**
 * Created by abbanerjee on 15/10/14.
 */
public class ReadPriceCSVTest {
    private static String filename = "ReadPriceTest";
    private static String baseLocation = "data";
    final Logger log = LoggerFactory.getLogger(StorePriceCSVTest.class);

    @Test
    public void canWriteAndReadLastCSV() {
        FileTool.deleteFile(filename);
        StorePriceCSV storage = new StorePriceCSV(baseLocation,filename);
        PriceData p1 = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.ONE);
        storage.store(p1);
        ReadPriceCSV readPriceCSV = new ReadPriceCSV(baseLocation,filename);
        PriceData priceData = readPriceCSV.read();
        log.info("priceData " + priceData);
        log.info("p1 " + p1.toString());
        assert (priceData.isSameFrontRowPricesAs(p1));
        FileTool.deleteFile(filename);
    }

    @BeforeMethod
    public void setUp() {

    }
}
