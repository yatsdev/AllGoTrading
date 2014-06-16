package org.yats.connectivity.oanda;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.oandarest.PriceFeed;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.MarketData;

public class PriceFeedTest {

    private static String CONFIG = "externalAccount=3173292\n" +
            "secret=37e6eba904c5a0599c364647b5bb39ed-49b4e985887799ca2a4fb4724b5ebda4\n" +
            "\n" +
            "OANDA_EURUSD=EUR_USD\n" +
            "OANDA_EURGBP=EUR_GBP\n" +
            "OANDA_EURCHF=EUR_CHF";

    @Test
    public void canReceiveEURUSD()
    {
        Tool.sleepFor(5000);
        assert (consumer.getReceived() > 0);
        priceFeed.shutdown();
        while(priceFeed.isRunning()) Tool.sleepABit();
        System.out.println("done.");
    }


    @BeforeMethod
    public void setUp() {
        PropertiesReader p = PropertiesReader.createFromConfigString(CONFIG);
        priceFeed = new PriceFeed(p);
        priceFeed.logon();
        consumer = new RateConsumer();
        priceFeed.subscribe("OANDA_EURUSD", consumer);
    }

    PriceFeed priceFeed;
    RateConsumer consumer;

    private class RateConsumer implements IConsumeMarketData {
        @Override
        public void onMarketData(MarketData marketData) {
            System.out.println(marketData.toString());
            received++;
        }

        @Override
        public UniqueId getConsumerId() {
            return UniqueId.create();
        }

        public int getReceived() {
            return received;
        }

        private RateConsumer() {
            received=0;
        }

        int received;
    }

} // class
