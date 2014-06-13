package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.connectivity.oanda.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trading.ProductList;

import java.io.IOException;

public class OandaClientMain {
    final Logger log = LoggerFactory.getLogger(FixClientMain.class);


    public void go() throws InterruptedException, IOException
    {

        /*
        config/FIXOrder_<username>.properties needs to provide the external account number of the user in the form:
        externalAccount=1234
         */
        String configFilename = Tool.getPersonalConfigFilename("config/Oanda");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);


        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed fxRates = PriceFeed.createFromPropertiesReader(prop);
        fxRates.setProductProvider(products);

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(fxRates);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);
        fxRates.logon();

        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);

//        OrderConnection orderConnection = OrderConnection.createFromConfigFile(configFIXOrderFilename);
//        orderConnection.setProductProvider(products);
//        orderConnection.logon();

//        strategyRunner.setOrderSender(orderConnection);
//        orderConnection.setReceiptConsumer(strategyRunner);
        marketToBusConnection.setOrderSender(strategyRunner);

        Thread.sleep(2000);

        marketToBusConnection.init();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        marketToBusConnection.shutdown();
        Thread.sleep(1000);

        System.exit(0);
    }

    public OandaClientMain() {
    }

    public static void main(String args[]) throws Exception {
        FixClientMain q = new FixClientMain();

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
