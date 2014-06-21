package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class RateConverterTest {

    public static final Decimal SAP_SIZE = Decimal.fromString("2");

    @Test
    public void canConvertPositionInEURToEUR() {
        Position p1InEUR = converter.convert(p1, TestMarketData.EUR_PID);
        assert(p1InEUR.isSize(TestMarketData.SAP_LAST.multiply(SAP_SIZE)));
    }

    @Test
    public void canConvertPositionInEURToUSD() {
        Position p1InUSD = converter.convert(p1, TestMarketData.USD_PID);
        Decimal expectedSize = TestMarketData.SAP_LAST.multiply(SAP_SIZE).multiply(TestMarketData.EURUSD_LAST);
        assert(p1InUSD.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToCHF() {
        Position p1InCHF = converter.convert(p1, TestMarketData.CHF_PID);


        Decimal expectedSize = TestMarketData.SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.EURUSD_LAST)
                .multiply(TestMarketData.USDCHF_LAST)
                ;

        assert(p1InCHF.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToGBPUsingInversionOfCurrencyPair() {
        Position p1InGBP = converter.convert(p1, TestMarketData.GBP_PID);
        Decimal expectedSize = TestMarketData.SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.EURUSD_LAST)
                .multiply(TestMarketData.GBPUSD_LAST.invert());
        assert(p1InGBP.isSize(expectedSize));
    }

    @Test
    public void canConvertPositionInEURToNZDUsingShortestChainOfConversions() {
        converter.onMarketData(TestMarketData.XAUUSD);
        converter.onMarketData(TestMarketData.XAUXAG);
        converter.onMarketData(TestMarketData.XAGNZD);
        converter.onMarketData(TestMarketData.AUDHKD);
        converter.onMarketData(TestMarketData.CADHKD);
        converter.onMarketData(TestMarketData.CADSGD);
        converter.onMarketData(TestMarketData.NZDCAD);
        converter.onMarketData(TestMarketData.SGDHKD);

        // long chains using AUDCHF are wrong anyways. lets make this market data corrupt artificially:
        converter.onMarketData(MarketData.createFromLast(TestMarketData.AUDCHF_PID,
                TestMarketData.AUDCHF_LAST.multiply(Decimal.fromString("0.9"))));


        Position p1InNZD = converter.convert(p1, TestMarketData.NZD_PID);
//
//        // some possible conversion chains:
//        // SAP->EUR->USD->XAU->XAG->NZD
//        // SAP->EUR->USD->CHF->AUD->HKD->CAD->NZD
//        // SAP->EUR->USD->CHF->AUD->HKD->CAD->SGD->NZD
//        // pitfall chain with a loop:
//        // SAP->EUR->USD->CHF->AUD->HKD->SGD->CAD->HKD->...
//
//
       Decimal expectedSize = TestMarketData.SAP_LAST
                .multiply(SAP_SIZE)
                .multiply(TestMarketData.EURUSD_LAST)
                .multiply(TestMarketData.XAUUSD_LAST.invert())
                .multiply(TestMarketData.XAUXAG_LAST.invert())
                .multiply(TestMarketData.XAGNZD_LAST)
                ;
        assert(p1InNZD.isSize(expectedSize));
    }


    @BeforeMethod
    public void setUp() {
        ProductList productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        converter = new RateConverter(productList);
        converter.onMarketData(TestMarketData.EURUSD);
        converter.onMarketData(TestMarketData.USDCHF);
        converter.onMarketData(TestMarketData.GBPUSD);
        converter.onMarketData(TestMarketData.SAP);
        p1 = new Position(TestMarketData.SAP_PID, SAP_SIZE);
    }


    Position p1;
    RateConverter converter;

} // class
