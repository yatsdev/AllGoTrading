package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.connectivity.oandarest.OandaApi;
import org.yats.trader.StrategyRunner;
import org.yats.trading.ProductList;

import java.io.IOException;

public class OandaApiMain {

    final Logger log = LoggerFactory.getLogger(OandaApiMain.class);

    public void go() throws InterruptedException, IOException
    {
        String configFilename = Tool.getPersonalConfigFilename("config/OandaApi");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        OandaApi oandaApi = new OandaApi(prop);
        oandaApi.setProductProvider(products);

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection(prop);

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(oandaApi);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);


        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);

        strategyRunner.setOrderSender(oandaApi);
        oandaApi.setReceiptConsumer(strategyRunner);
        marketToBusConnection.setOrderSender(strategyRunner);

        oandaApi.logon();
        marketToBusConnection.init();

//        strategyRunner.subscribe("OANDA_EURUSD", this);
        Thread.sleep(2000);

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        marketToBusConnection.shutdown();
        oandaApi.shutdown();
        Thread.sleep(1000);

        System.exit(0);
    }

    public OandaApiMain() {
    }

    public static void main(String args[]) throws Exception {
        OandaApiMain q = new OandaApiMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

} // class
