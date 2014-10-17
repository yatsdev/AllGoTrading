package org.yats.trading;

import org.yats.common.Decimal;

public class TestPriceData {

    public static final String TEST_EUR_PID = "TEST_CCY_EUR";
    public static final String TEST_USD_PID = "TEST_CCY_USD";
    public static final String TEST_CHF_PID = "TEST_CCY_CHF";
    public static final String TEST_GBP_PID = "TEST_CCY_GBP";
    public static final String TEST_HKD_PID = "TEST_CCY_HKD";
    public static final String TEST_NZD_PID = "TEST_CCY_NZD";

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

    public static final String TEST_IBM_PID ="TEST_IBM";
    public static final String TEST_SAP_PID ="TEST_SAP";
    public static final Decimal TEST_SAP_LAST = Decimal.fromString("85");
    public static final String TEST_SAP_SYMBOL ="SAP";

    public final static PriceData TEST_EURUSD = PriceData.createFromLast(TEST_EURUSD_PID, TEST_EURUSD_LAST);
    public final static PriceData TEST_USDCHF = PriceData.createFromLast(TEST_USDCHF_PID, TEST_USDCHF_LAST);
    public final static PriceData TEST_GBPUSD = PriceData.createFromLast(TEST_GBPUSD_PID, TEST_GBPUSD_LAST);
    public final static PriceData TEST_SAP = PriceData.createFromLast(TEST_SAP_PID, TEST_SAP_LAST);

    public final static PriceData TEST_XAUUSD = PriceData.createFromLast(TEST_XAUUSD_PID, TEST_XAUUSD_LAST);
    public final static PriceData TEST_XAUXAG = PriceData.createFromLast(TEST_XAUXAG_PID, TEST_XAUXAG_LAST);
    public final static PriceData TEST_XAGUSD = PriceData.createFromLast(TEST_XAGUSD_PID, TEST_XAGUSD_LAST);
    public final static PriceData TEST_XAGNZD = PriceData.createFromLast(TEST_XAGNZD_PID, TEST_XAGNZD_LAST);
    public final static PriceData TEST_AUDCHF = PriceData.createFromLast(TEST_AUDCHF_PID, TEST_AUDCHF_LAST);
    public final static PriceData TEST_AUDHKD = PriceData.createFromLast(TEST_AUDHKD_PID, TEST_AUDHKD_LAST);
    public final static PriceData TEST_CADHKD = PriceData.createFromLast(TEST_CADHKD_PID, TEST_CADHKD_LAST);
    public final static PriceData TEST_CADSGD = PriceData.createFromLast(TEST_CADSGD_PID, TEST_CADSGD_LAST);
    public final static PriceData TEST_NZDCAD = PriceData.createFromLast(TEST_NZDCAD_PID, TEST_NZDCAD_LAST);
    public final static PriceData TEST_SGDHKD = PriceData.createFromLast(TEST_SGDHKD_PID, TEST_SGDHKD_LAST);

    public final static PriceData PRODUCT1_DATA = PriceData.createFromLast(ProductTest.TEST_PRODUCT1_ID, Decimal.ONE);
    public final static PriceData PRODUCT2_DATA = PriceData.createFromLast(ProductTest.TEST_PRODUCT2_ID, Decimal.ONE);
    public final static PriceData PRODUCT3_DATA = PriceData.createFromLast(ProductTest.TEST_PRODUCT3_ID, Decimal.ONE);

} // class
