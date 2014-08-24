package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class RateConverterTest {

    public static final Decimal SAP_SIZE = Decimal.fromString("2");
    public static final Decimal HKD_SIZE = Decimal.fromString("1000");

    @Test
    public void canConvertPositionInEURToEUR() {
        Position p1InEUR = converter.convert(p1, TestMarketData.TEST_EUR_PID);
        assert(p1InEUR.isSize(TestMarketData.TEST_SAP_LAST.multiply(SAP_SIZE)));
    }

    @Test
    public void canConvertPositionInEURToUSD() {
        Position p1InUSD = converter.convert(p1, TestMarketData.TEST_USD_PID);
        Decimal expectedSize = TestMarketData.TEST_SAP_LAST.multiply(SAP_SIZE).multiply(TestMarketData.TEST_EURUSD_LAST);
        assert(p1InUSD.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToCHF() {
        Position p1InCHF = converter.convert(p1, TestMarketData.TEST_CHF_PID);
        Decimal expectedSize = TestMarketData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.TEST_EURUSD_LAST)
                .multiply(TestMarketData.TEST_USDCHF_LAST)
                ;
        assert(p1InCHF.isSize(expectedSize));
    }

    @Test
    public void canCache() {
        Position p1InCHF = converter.convert(p1, TestMarketData.TEST_CHF_PID);
        assert(converter.getCacheHits()==0);
        assert(converter.getCacheSize()==1);
        Position p2InCHF = converter.convert(p1, TestMarketData.TEST_CHF_PID);
        assert(converter.getCacheHits()==1);
        assert(converter.getCacheSize()==1);

        Decimal expectedSize = TestMarketData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.TEST_EURUSD_LAST)
                .multiply(TestMarketData.TEST_USDCHF_LAST)
                ;
        assert(p2InCHF.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToGBPUsingInversionOfCurrencyPair() {
        Position p1InGBP = converter.convert(p1, TestMarketData.TEST_GBP_PID);
        Decimal expectedSize = TestMarketData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.TEST_EURUSD_LAST)
                .multiply(TestMarketData.TEST_GBPUSD_LAST.invert())
                ;
        assert(p1InGBP.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToNZDUsingShortestChainOfConversions() {
        converter.onMarketData(TestMarketData.TEST_XAUUSD);
        converter.onMarketData(TestMarketData.TEST_XAUXAG);
        converter.onMarketData(TestMarketData.TEST_XAGNZD);
        converter.onMarketData(TestMarketData.TEST_AUDHKD);
        converter.onMarketData(TestMarketData.TEST_CADHKD);
        converter.onMarketData(TestMarketData.TEST_CADSGD);
        converter.onMarketData(TestMarketData.TEST_NZDCAD);
        converter.onMarketData(TestMarketData.TEST_SGDHKD);

        // long chains using AUDCHF are wrong anyways. lets make this market data corrupt artificially:
        converter.onMarketData(MarketData.createFromLast(TestMarketData.TEST_AUDCHF_PID,
                TestMarketData.TEST_AUDCHF_LAST.multiply(Decimal.fromString("0.9"))));

        Position p1InNZD = converter.convert(p1, TestMarketData.TEST_NZD_PID);

        // some possible conversion chains:
        // TEST_SAP->EUR->USD->XAU->XAG->NZD
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->CAD->NZD
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->CAD->SGD->NZD
        // pitfall chain with a loop:
        // TEST_SAP->EUR->USD->CHF->AUD->HKD->SGD->CAD->HKD->...


        Decimal expectedSize = TestMarketData.TEST_SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.TEST_EURUSD_LAST)
                .multiply(TestMarketData.TEST_XAUUSD_LAST.invert())
                .multiply(TestMarketData.TEST_XAUXAG_LAST)
                .multiply(TestMarketData.TEST_XAGNZD_LAST)
                ;
        assert(p1InNZD.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInHKDToSAPUsingShortestChainOfConversions() {
        converter.onMarketData(TestMarketData.TEST_XAUUSD);
        converter.onMarketData(TestMarketData.TEST_XAUXAG);
        converter.onMarketData(TestMarketData.TEST_XAGNZD);
        converter.onMarketData(TestMarketData.TEST_AUDHKD);
        converter.onMarketData(TestMarketData.TEST_CADHKD);
        converter.onMarketData(TestMarketData.TEST_CADSGD);
        converter.onMarketData(TestMarketData.TEST_NZDCAD);
        converter.onMarketData(TestMarketData.TEST_SGDHKD);

        Position p2InSAP = converter.convert(p2, TestMarketData.TEST_SAP_PID);

        Decimal expectedSize = HKD_SIZE
                .multiply(TestMarketData.TEST_CADHKD_LAST.invert())
                .multiply(TestMarketData.TEST_NZDCAD_LAST.invert())
                .multiply(TestMarketData.TEST_XAGNZD_LAST.invert())
                .multiply(TestMarketData.TEST_XAUXAG_LAST.invert())
                .multiply(TestMarketData.TEST_XAUUSD_LAST)
                .multiply(TestMarketData.TEST_EURUSD_LAST.invert())
                .multiply(TestMarketData.TEST_SAP_LAST.invert())
                ;

        assert(p2InSAP.isSize(expectedSize));
        assert(converter.getCacheSize()==1);
        assert(converter.isChainInCache(p2.getProductId(), TestMarketData.TEST_SAP_PID));
    }

    @BeforeMethod
    public void setUp() {
        ProductList productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        converter = new RateConverter(productList);
        converter.onMarketData(TestMarketData.TEST_EURUSD);
        converter.onMarketData(TestMarketData.TEST_USDCHF);
        converter.onMarketData(TestMarketData.TEST_GBPUSD);
        converter.onMarketData(TestMarketData.TEST_SAP);
        p1 = new Position(TestMarketData.TEST_SAP_PID, SAP_SIZE);
        p2 = new Position(TestMarketData.TEST_HKD_PID, HKD_SIZE);
    }


    Position p1;
    Position p2;
    RateConverter converter;

} // class
