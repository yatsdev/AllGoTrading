package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class RateConverterTest {

    public static final Decimal SAP_SIZE = Decimal.fromString("2");
    public static final Decimal HKD_SIZE = Decimal.fromString("1000");

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInEURToEUR() {
        Position p1InEUR = converter.convert(p1, TestPriceData.TEST_EUR_PID);
        assert(p1InEUR.isSize(TestPriceData.TEST_SAP_LAST.multiply(SAP_SIZE)));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInEURToUSD() {
        Position p1InUSD = converter.convert(p1, TestPriceData.TEST_USD_PID);
        Decimal expectedSize = TestPriceData.TEST_SAP_LAST.multiply(SAP_SIZE).multiply(TestPriceData.TEST_EURUSD_LAST);
        assert(p1InUSD.isSize(expectedSize));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInEURToCHF() {
        Position p1InCHF = converter.convert(p1, TestPriceData.TEST_CHF_PID);
        Decimal expectedSize = TestPriceData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestPriceData.TEST_EURUSD_LAST)
                .multiply(TestPriceData.TEST_USDCHF_LAST)
                ;
        assert(p1InCHF.isSize(expectedSize));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canCache() {
        Position p1InCHF = converter.convert(p1, TestPriceData.TEST_CHF_PID);
        assert(converter.getCacheHits()==0);
        assert(converter.getCacheSize()==1);
        Position p2InCHF = converter.convert(p1, TestPriceData.TEST_CHF_PID);
        assert(converter.getCacheHits()==1);
        assert(converter.getCacheSize()==1);

        Decimal expectedSize = TestPriceData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestPriceData.TEST_EURUSD_LAST)
                .multiply(TestPriceData.TEST_USDCHF_LAST)
                ;
        assert(p2InCHF.isSize(expectedSize));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInEURToGBPUsingInversionOfCurrencyPair() {
        Position p1InGBP = converter.convert(p1, TestPriceData.TEST_GBP_PID);
        Decimal expectedSize = TestPriceData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestPriceData.TEST_EURUSD_LAST)
                .multiply(TestPriceData.TEST_GBPUSD_LAST.invert())
                ;
        assert(p1InGBP.isSize(expectedSize));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInEURToNZDUsingShortestChainOfConversions() {
        converter.onPriceData(TestPriceData.TEST_XAUUSD);
        converter.onPriceData(TestPriceData.TEST_XAUXAG);
        converter.onPriceData(TestPriceData.TEST_XAGNZD);
        converter.onPriceData(TestPriceData.TEST_AUDHKD);
        converter.onPriceData(TestPriceData.TEST_CADHKD);
        converter.onPriceData(TestPriceData.TEST_CADSGD);
        converter.onPriceData(TestPriceData.TEST_NZDCAD);
        converter.onPriceData(TestPriceData.TEST_SGDHKD);

        // long chains using AUDCHF are wrong anyways. lets make this market data corrupt artificially:
        converter.onPriceData(PriceData.createFromLast(TestPriceData.TEST_AUDCHF_PID,
                TestPriceData.TEST_AUDCHF_LAST.multiply(Decimal.fromString("0.9"))));

        Position p1InNZD = converter.convert(p1, TestPriceData.TEST_NZD_PID);

        // some possible conversion chains:
        // TEST_SAP->EUR->USD->XAU->XAG->NZD
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->CAD->NZD
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->CAD->SGD->NZD
        // pitfall chain with a loop:
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->SGD->CAD->HKD->...


        Decimal expectedSize = TestPriceData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestPriceData.TEST_EURUSD_LAST)
                .multiply(TestPriceData.TEST_XAUUSD_LAST.invert())
                .multiply(TestPriceData.TEST_XAUXAG_LAST)
                .multiply(TestPriceData.TEST_XAGNZD_LAST)
                ;
        assert(p1InNZD.isSize(expectedSize));
    }

    @Test(groups = { "integration", "inMemory" })
    public void canConvertPositionInHKDToSAPUsingShortestChainOfConversions() {
        converter.onPriceData(TestPriceData.TEST_XAUUSD);
        converter.onPriceData(TestPriceData.TEST_XAUXAG);
        converter.onPriceData(TestPriceData.TEST_XAGNZD);
        converter.onPriceData(TestPriceData.TEST_AUDHKD);
        converter.onPriceData(TestPriceData.TEST_CADHKD);
        converter.onPriceData(TestPriceData.TEST_CADSGD);
        converter.onPriceData(TestPriceData.TEST_NZDCAD);
        converter.onPriceData(TestPriceData.TEST_SGDHKD);

        Position p2InSAP = converter.convert(p2, TestPriceData.TEST_SAP_PID);

        Decimal expectedSize = HKD_SIZE
                .multiply(TestPriceData.TEST_CADHKD_LAST.invert())
                .multiply(TestPriceData.TEST_NZDCAD_LAST.invert())
                .multiply(TestPriceData.TEST_XAGNZD_LAST.invert())
                .multiply(TestPriceData.TEST_XAUXAG_LAST.invert())
                .multiply(TestPriceData.TEST_XAUUSD_LAST)
                .multiply(TestPriceData.TEST_EURUSD_LAST.invert())
                .multiply(TestPriceData.TEST_SAP_LAST.invert())
                ;

        assert(p2InSAP.isSize(expectedSize));
        assert(converter.getCacheSize()==1);
        assert(converter.isChainInCache(p2.getProductId(), TestPriceData.TEST_SAP_PID));
    }

    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setUp() {
        ProductList productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        converter = new RateConverter(productList);
        converter.onPriceData(TestPriceData.TEST_EURUSD);
        converter.onPriceData(TestPriceData.TEST_USDCHF);
        converter.onPriceData(TestPriceData.TEST_GBPUSD);
        converter.onPriceData(TestPriceData.TEST_SAP);
        p1 = new Position(TestPriceData.TEST_SAP_PID, SAP_SIZE);
        p2 = new Position(TestPriceData.TEST_HKD_PID, HKD_SIZE);
    }


    Position p1;
    Position p2;
    RateConverter converter;

} // class
