package org.yats.trading;

import org.yats.common.Decimal;

public class TestMarketData {

    public static final String EUR_PID = "CCY_EUR";
    public static final String USD_PID = "CCY_USD";
    public static final String CHF_PID = "CCY_CHF";
    public static final String GBP_PID = "CCY_GBP";
    public static final String SGD_PID = "CCY_SGD";
    public static final String HKD_PID = "CCY_HKD";
    public static final String XAG_PID = "CCY_XAG";
    public static final String NZD_PID = "CCY_NZD";
    public static final String CAD_PID = "CCY_CAD";
    public static final String XAU_PID = "CCY_XAU";
    public static final String AUD_PID = "CCY_AUD";

    public static final String EURUSD_PID = "OANDA_EURUSD";
    public static final Decimal EURUSD_LAST = Decimal.fromString("1.35398");

    public static final String USDCHF_PID = "OANDA_USDCHF";
    public static final Decimal USDCHF_LAST = Decimal.fromString("0.89927");

    public static final String GBPUSD_PID = "OANDA_GBPUSD";
    public static final Decimal GBPUSD_LAST = Decimal.fromString("1.68178");

    public static final String XAUUSD_PID = "OANDA_XAUUSD";
    public static final Decimal XAUUSD_LAST = Decimal.fromString("1265.01");

    public static final String XAUXAG_PID = "OANDA_XAUXAG";
    public static final Decimal XAUXAG_LAST = Decimal.fromString("65.4659");

    public static final String XAGUSD_PID = "OANDA_XAGUSD";
    public static final Decimal XAGUSD_LAST = Decimal.fromString("19.2968");

    public static final String XAGNZD_PID = "OANDA_XAGNZD";
    public static final Decimal XAGNZD_LAST = Decimal.fromString("19.2968");

    public static final String AUDCHF_PID = "OANDA_AUDCHF";
    public static final Decimal AUDCHF_LAST = Decimal.fromString("0.84530");

    public static final String AUDHKD_PID = "OANDA_AUDHKD";
    public static final Decimal AUDHKD_LAST = Decimal.fromString("7.28625");

    public static final String CADHKD_PID = "OANDA_CADHKD";
    public static final Decimal CADHKD_LAST = Decimal.fromString("7.13713");

    public static final String CADSGD_PID = "OANDA_CADSGD";
    public static final Decimal CADSGD_LAST = Decimal.fromString("1.14986");

    public static final String NZDCAD_PID = "OANDA_NZDCAD";
    public static final Decimal NZDCAD_LAST = Decimal.fromString("0.94028");

    public static final String SGDHKD_PID = "OANDA_SGDHKD";
    public static final Decimal SGDHKD_LAST = Decimal.fromString("6.20477");

    public static final String SAP_PID ="4663789";
    public static final Decimal SAP_LAST = Decimal.fromString("85");
    public static final String SAP_SYMBOL ="SAP";

    public final static MarketData EURUSD = MarketData.createFromLast(EURUSD_PID, EURUSD_LAST);
    public final static MarketData USDCHF = MarketData.createFromLast(USDCHF_PID, USDCHF_LAST);
    public final static MarketData GBPUSD = MarketData.createFromLast(GBPUSD_PID, GBPUSD_LAST);
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

} // class
