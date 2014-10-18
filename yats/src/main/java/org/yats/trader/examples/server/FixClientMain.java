package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.fix.OrderConnection;
import org.yats.connectivity.fix.PriceFeed;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.trader.StrategyRunner;
import org.yats.trading.ProductList;

import java.io.IOException;

/*
    This server provides a link between an external FIX server and the internal message bus. It passes
    subscriptions and orders from the message bus to the FIX server and prices and receipts
    back to the to the message bus.
 */

public class FixClientMain {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory

    final Logger log = LoggerFactory.getLogger(FixClientMain.class);


    public void go() throws InterruptedException, IOException
    {

        /*
        config/FIXOrder_<username>.properties needs to provide the external account number of the user in the form:
        externalAccount=1234
         */
        final String className = FixClientMain.class.getSimpleName();
        String configFIXOrderFilename = Tool.getPersonalConfigFilename("config","FIXOrder");
        String configFIXPriceFilename = Tool.getPersonalConfigFilename("config","FIXPrice");
        String configFIXFilename = Tool.getPersonalConfigFilename("config",className);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed priceFeed = PriceFeed.createFromConfigFile(configFIXPriceFilename);
        priceFeed.setProductProvider(products);

        IProvideProperties prop = PropertiesReader.createFromConfigFile(configFIXFilename);
        MarketToBusConnection marketToBusConnection = new MarketToBusConnection(prop);

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(marketToBusConnection);
//        strategyRunner.setProductProvider(products);
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
