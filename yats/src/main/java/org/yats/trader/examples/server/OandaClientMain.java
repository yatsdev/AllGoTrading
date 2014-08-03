package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.connectivity.oandarest.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.MarketData;
import org.yats.trading.ProductList;

import java.io.IOException;

/*
  Use template in
  config/OandaConnection_template.properties
  and create your personal config file.
 */

public class OandaClientMain implements IConsumeMarketData {

    final Logger log = LoggerFactory.getLogger(FixClientMain.class);

    public void go() throws InterruptedException, IOException
    {
        String configFilename = Tool.getPersonalConfigFilename("config/OandaConnection");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed oandaFeed = PriceFeed.createFromPropertiesReader(prop);
        oandaFeed.setProductProvider(products);

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(oandaFeed);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);
        oandaFeed.logon();

        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);

//        OrderConnection orderConnection = OrderConnection.createFromProperties(configFIXOrderFilename);
//        orderConnection.setProductProvider(products);
//        orderConnection.logon();

//        strategyRunner.setOrderSender(orderConnection);
//        orderConnection.setReceiptConsumer(strategyRunner);
        marketToBusConnection.setOrderSender(strategyRunner);

        strategyRunner.subscribe("OANDA_EURUSD", this);
        Thread.sleep(2000);

        marketToBusConnection.init();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        marketToBusConnection.shutdown();
        oandaFeed.shutdown();
        Thread.sleep(1000);

        System.exit(0);
    }

    public OandaClientMain() {
    }

    public static void main(String args[]) throws Exception {
        OandaClientMain q = new OandaClientMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    @Override
    public void onMarketData(MarketData marketData) {
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

} // class
