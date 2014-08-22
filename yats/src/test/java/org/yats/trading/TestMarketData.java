package org.yats.trading;

import org.yats.common.Decimal;

public class TestMarketData {

    public static final String TEST_EUR_PID = "TEST_CCY_EUR";
    public static final String TEST_USD_PID = "TEST_CCY_USD";
    public static final String TEST_CHF_PID = "TEST_CCY_CHF";
    public static final String TEST_GBP_PID = "TEST_CCY_GBP";
    public static final String TEST_SGD_PID = "TEST_CCY_SGD";
    public static final String TEST_HKD_PID = "TEST_CCY_HKD";
    public static final String TEST_XAG_PID = "TEST_CCY_XAG";
    public static final String TEST_NZD_PID = "TEST_CCY_NZD";
    public static final String TEST_CAD_PID = "TEST_CCY_CAD";
    public static final String TEST_XAU_PID = "TEST_CCY_XAU";
    public static final String TEST_AUD_PID = "TEST_CCY_AUD";

    public static final String TEST_EURUSD_PID = "TEST_EURUSD";
    public static final Decimal TEST_EURUSD_LAST = Decimal.fromString("1.35398");

    public static final String TEST_USDCHF_PID = "TEST_USDCHF";
    public static final Decimal TEST_USDCHF_LAST = Decimal.fromString("0.89927");

    public static final String TEST_GBPUSD_PID = "TEST_GBPUSD";
    public static final Decimal TEST_GBPUSD_LAST = Decimal.fromString("1.68178");

    public static final String TEST_XAUUSD_PID = "TEST_XAUUSD";
    public static final Decimal TEST_XAUUSD_LAST = Decimal.fromString("1,265.01");

    public static final String TEST_XAUXAG_PID = "TEST_XAUXAG";
    public static final Decimal TEST_XAUXAG_LAST = Decimal.fromString("65.4659");

    public static final String TEST_XAGUSD_PID = "TEST_XAGUSD";
    public static final Decimal TEST_XAGUSD_LAST = Decimal.fromString("19.2968");

    public static final String TEST_XAGNZD_PID = "TEST_XAGNZD";
    public static final Decimal TEST_XAGNZD_LAST = Decimal.fromString("19.2968");

    public static final String TEST_AUDCHF_PID = "TEST_AUDCHF";
    public static final Decimal TEST_AUDCHF_LAST = Decimal.fromString("0.84530");

    public static final String TEST_AUDHKD_PID = "TEST_AUDHKD";
    public static final Decimal TEST_AUDHKD_LAST = Decimal.fromString("7.28625");

    public static final String TEST_CADHKD_PID = "TEST_CADHKD";
    public static final Decimal TEST_CADHKD_LAST = Decimal.fromString("7.13713");

    public static final String TEST_CADSGD_PID = "TEST_CADSGD";
    public static final Decimal TEST_CADSGD_LAST = Decimal.fromString("1.14986");

    public static final String TEST_NZDCAD_PID = "TEST_NZDCAD";
    public static final Decimal TEST_NZDCAD_LAST = Decimal.fromString("0.94028");

    public static final String TEST_SGDHKD_PID = "TEST_SGDHKD";
    public static final Decimal TEST_SGDHKD_LAST = Decimal.fromString("6.20477");

    public static final String TEST_SAP_PID ="TEST_SAP";
    public static final Decimal TEST_SAP_LAST = Decimal.fromString("85");
    public static final String TEST_SAP_SYMBOL ="SAP";

    public final static MarketData TEST_EURUSD = MarketData.createFromLast(TEST_EURUSD_PID, TEST_EURUSD_LAST);
    public final static MarketData TEST_USDCHF = MarketData.createFromLast(TEST_USDCHF_PID, TEST_USDCHF_LAST);
    public final static MarketData TEST_GBPUSD = MarketData.createFromLast(TEST_GBPUSD_PID, TEST_GBPUSD_LAST);
    public final static MarketData TEST_SAP = MarketData.createFromLast(TEST_SAP_PID, TEST_SAP_LAST);

    public final static MarketData TEST_XAUUSD = MarketData.createFromLast(TEST_XAUUSD_PID, TEST_XAUUSD_LAST);
    public final static MarketData TEST_XAUXAG = MarketData.createFromLast(TEST_XAUXAG_PID, TEST_XAUXAG_LAST);
    public final static MarketData TEST_XAGUSD = MarketData.createFromLast(TEST_XAGUSD_PID, TEST_XAGUSD_LAST);
    public final static MarketData TEST_XAGNZD = MarketData.createFromLast(TEST_XAGNZD_PID, TEST_XAGNZD_LAST);
    public final static MarketData TEST_AUDCHF = MarketData.createFromLast(TEST_AUDCHF_PID, TEST_AUDCHF_LAST);
    public final static MarketData TEST_AUDHKD = MarketData.createFromLast(TEST_AUDHKD_PID, TEST_AUDHKD_LAST);
    public final static MarketData TEST_CADHKD = MarketData.createFromLast(TEST_CADHKD_PID, TEST_CADHKD_LAST);
    public final static MarketData TEST_CADSGD = MarketData.createFromLast(TEST_CADSGD_PID, TEST_CADSGD_LAST);
    public final static MarketData TEST_NZDCAD = MarketData.createFromLast(TEST_NZDCAD_PID, TEST_NZDCAD_LAST);
    public final static MarketData TEST_SGDHKD = MarketData.createFromLast(TEST_SGDHKD_PID, TEST_SGDHKD_LAST);

    public final static MarketData PRODUCT1_DATA = MarketData.createFromLast(ProductTest.PRODUCT1.getProductId(), Decimal.ONE);
    public final static MarketData PRODUCT2_DATA = MarketData.createFromLast(ProductTest.PRODUCT2.getProductId(), Decimal.ONE);
    public final static MarketData PRODUCT3_DATA = MarketData.createFromLast(ProductTest.PRODUCT3.getProductId(), Decimal.ONE);

} // class
