package org.yats.connectivity.oanda;

import org.testng.annotations.BeforeMethod;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.connectivity.oandarest.PriceFeed;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;

public class PriceFeedTest {

    private static String CONFIG = "externalAccount=3173292\n" +
            "secret=37e6eba904c5a0599c364647b5bb39ed-49b4e985887799ca2a4fb4724b5ebda4\n" +
            "\n" +
            "OANDA_EURUSD=EUR_USD\n" +
            "OANDA_EURGBP=EUR_GBP\n" +
            "OANDA_EURCHF=EUR_CHF";

//    Not really a test since it takes too long and depends on external connection.
//    @Test
//    public void canReceiveEURUSD()
//    {
//        Tool.sleepFor(5000);
//        assert (consumer.getReceived() > 0);
//        priceFeed.close();
//        while(priceFeed.isRunning()) Tool.sleepABit();
//        System.out.println("done.");
//    }


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

    private class RateConsumer implements IConsumePriceData {
        @Override
        public void onPriceData(PriceData priceData) {
            System.out.println(priceData.toString());
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
