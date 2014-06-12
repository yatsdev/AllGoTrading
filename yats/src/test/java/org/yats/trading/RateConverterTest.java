package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.yats.common.Decimal;

public class RateConverterTest {

    public static final Decimal SAP_SIZE = Decimal.fromString("2");

//    @Test
//    public void canConvertPositionInEURToEUR() {
//        Position p1InEUR = converter.convert(p1, MarketDataTest.EUR_PID);
//        assert(p1InEUR.isSize(MarketDataTest.SAP_LAST.multiply(SAP_SIZE)));
//    }
//
//    @Test
//    public void canConvertPositionInEURToUSD() {
//        Position p1InUSD = converter.convert(p1, MarketDataTest.USD_PID);
//        Decimal expectedSize = MarketDataTest.SAP_LAST.multiply(SAP_SIZE).multiply(MarketDataTest.EUR_USD_LAST);
//        assert(p1InUSD.isSize(expectedSize));
//    }
//
//    @Test
//    public void canConvertPositionInEURToCHF() {
//        Position p1InCHF = converter.convert(p1, MarketDataTest.CHF_PID);
//        Decimal expectedSize = MarketDataTest.SAP_LAST
//                .multiply(SAP_SIZE)
//                .multiply(MarketDataTest.EUR_USD_LAST)
//                .multiply(MarketDataTest.USD_CHF_LAST)
//                ;
//        assert(p1InCHF.isSize(expectedSize));
//    }

    @BeforeMethod
    public void setUp() {
        ProductList productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        converter = new RateConverter(productList);
        converter.onMarketData(MarketDataTest.EURUSD);
        converter.onMarketData(MarketDataTest.USDCHF);
        converter.onMarketData(MarketDataTest.SAP);
        p1 = new Position(MarketDataTest.SAP_PID, SAP_SIZE);
    }


    Position p1;
    RateConverter converter;

} // class