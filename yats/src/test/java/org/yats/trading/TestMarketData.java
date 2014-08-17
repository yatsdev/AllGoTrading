package org.yats.trading;

import org.yats.common.Decimal;

public class TestMarketData {

    public static final String EUR_PID = "TEST_CCY_EUR";
    public static final String USD_PID = "TEST_CCY_USD";
    public static final String CHF_PID = "TEST_CCY_CHF";
    public static final String GBP_PID = "TEST_CCY_GBP";
    public static final String SGD_PID = "TEST_CCY_SGD";
    public static final String HKD_PID = "TEST_CCY_HKD";
    public static final String XAG_PID = "TEST_CCY_XAG";
    public static final String NZD_PID = "TEST_CCY_NZD";
    public static final String CAD_PID = "TEST_CCY_CAD";
    public static final String XAU_PID = "TEST_CCY_XAU";
    public static final String AUD_PID = "TEST_CCY_AUD";

    public static final String EURUSD_PID = "TEST_EURUSD";
    public static final Decimal EURUSD_LAST = Decimal.fromString("1.35398");

    public static final String USDCHF_PID = "TEST_USDCHF";
    public static final Decimal USDCHF_LAST = Decimal.fromString("0.89927");

    public static final String GBPUSD_PID = "TEST_GBPUSD";
    public static final Decimal GBPUSD_LAST = Decimal.fromString("1.68178");

    public static final String XAUUSD_PID = "TEST_XAUUSD";
    public static final Decimal XAUUSD_LAST = Decimal.fromString("1,265.01");

    public static final String XAUXAG_PID = "TEST_XAUXAG";
    public static final Decimal XAUXAG_LAST = Decimal.fromString("65.4659");

    public static final String XAGUSD_PID = "TEST_XAGUSD";
    public static final Decimal XAGUSD_LAST = Decimal.fromString("19.2968");

    public static final String XAGNZD_PID = "TEST_XAGNZD";
    public static final Decimal XAGNZD_LAST = Decimal.fromString("19.2968");

    public static final String AUDCHF_PID = "TEST_AUDCHF";
    public static final Decimal AUDCHF_LAST = Decimal.fromString("0.84530");

    public static final String AUDHKD_PID = "TEST_AUDHKD";
    public static final Decimal AUDHKD_LAST = Decimal.fromString("7.28625");

    public static final String CADHKD_PID = "TEST_CADHKD";
    public static final Decimal CADHKD_LAST = Decimal.fromString("7.13713");

    public static final String CADSGD_PID = "TEST_CADSGD";
    public static final Decimal CADSGD_LAST = Decimal.fromString("1.14986");

    public static final String NZDCAD_PID = "TEST_NZDCAD";
    public static final Decimal NZDCAD_LAST = Decimal.fromString("0.94028");

    public static final String SGDHKD_PID = "TEST_SGDHKD";
    public static final Decimal SGDHKD_LAST = Decimal.fromString("6.20477");

    public static final String SAP_PID ="TEST_SAP";
    public static final Decimal SAP_LAST = Decimal.fromString("85");
    public static final String SAP_SYMBOL ="SAP";

    public final static MarketData TEST_EURUSD = MarketData.createFromLast(EURUSD_PID, EURUSD_LAST);
    public final static MarketData USDCHF = MarketData.createFromLast(USDCHF_PID, USDCHF_LAST);
    public final static MarketData TEST_GBPUSD = MarketData.createFromLast(GBPUSD_PID, GBPUSD_LAST);
    public final static MarketData SAP = MarketData.createFromLast(SAP_PID, SAP_LAST);

    public final static MarketData XAUUSD = MarketData.createFromLast(XAUUSD_PID, XAUUSD_LAST);
    public final static MarketData XAUXAG = MarketData.createFromLast(XAUXAG_PID, XAUXAG_LAST);
    public final static MarketData XAGUSD = MarketData.createFromLast(XAGUSD_PID, XAGUSD_LAST);
    public final static MarketData XAGNZD = MarketData.createFromLast(XAGNZD_PID, XAGNZD_LAST);
    public final static MarketData AUDCHF = MarketData.createFromLast(AUDCHF_PID, AUDCHF_LAST);
    public final static MarketData AUDHKD = MarketData.createFromLast(AUDHKD_PID, AUDHKD_LAST);
    public final static MarketData CADHKD = MarketData.createFromLast(CADHKD_PID, CADHKD_LAST);
    public final static MarketData CADSGD = MarketData.createFromLast(CADSGD_PID, CADSGD_LAST);
    public final static MarketData NZDCAD = MarketData.createFromLast(NZDCAD_PID, NZDCAD_LAST);
    public final static MarketData SGDHKD = MarketData.createFromLast(SGDHKD_PID, SGDHKD_LAST);

    public final static MarketData PRODUCT1_DATA = MarketData.createFromLast(ProductTest.PRODUCT1.getProductId(), Decimal.ONE);
    public final static MarketData PRODUCT2_DATA = MarketData.createFromLast(ProductTest.PRODUCT2.getProductId(), Decimal.ONE);
    public final static MarketData PRODUCT3_DATA = MarketData.createFromLast(ProductTest.PRODUCT3.getProductId(), Decimal.ONE);

} // class
