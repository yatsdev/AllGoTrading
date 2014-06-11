package org.yats.trading;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

public class MarketDataTest {

    public static final String EUR_PID = "CCY007";
    public static final String USD_PID = "CCY001";
    public static final String CHF_PID = "CCY004";

    public static final String EURUSD_PID = "OANDA0001";
    public static final Decimal EUR_USD_LAST = Decimal.fromString("1.35");

    public static final String USDCHF_PID = "OANDA0004";
    public static final Decimal USD_CHF_LAST = Decimal.fromString("0.9");


    public static final String SAP_PID ="4663789";
    public static final Decimal SAP_LAST = Decimal.fromString("85");


    public final static MarketData EURUSD = new MarketData(DateTime.now(DateTimeZone.UTC), EURUSD_PID,
            EUR_USD_LAST.subtract(Decimal.CENT), EUR_USD_LAST.add(Decimal.CENT), EUR_USD_LAST,
            Decimal.ONE, Decimal.ONE, Decimal.ONE);

    public final static MarketData USDCHF = new MarketData(DateTime.now(DateTimeZone.UTC), USDCHF_PID,
            USD_CHF_LAST.subtract(Decimal.CENT), USD_CHF_LAST.add(Decimal.CENT), USD_CHF_LAST,
            Decimal.ONE, Decimal.ONE, Decimal.ONE);

    public final static MarketData SAP = new MarketData(DateTime.now(DateTimeZone.UTC), SAP_PID,
            SAP_LAST.subtract(Decimal.CENT), SAP_LAST.add(Decimal.CENT), SAP_LAST,
            Decimal.ONE, Decimal.ONE, Decimal.ONE);

    @Test
    public void can() {
    }

    @BeforeMethod
    public void setUp() {

    }

} // class
