package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.MarketToBusConnection;
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

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed priceFeed = PriceFeed.createFromConfigFile(configFIXPriceFilename);
        priceFeed.setProductProvider(products);

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);
        priceFeed.logon();

        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);

        OrderConnection orderConnection = OrderConnection.createFromConfigFile(configFIXOrderFilename);
        orderConnection.setProductProvider(products);
        orderConnection.logon();

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
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


    public FixClientMain() {
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
