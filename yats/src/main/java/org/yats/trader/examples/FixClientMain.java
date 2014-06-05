package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.FileTool;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.fix.OrderConnection;
import org.yats.connectivity.fix.PriceFeed;
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
//        PriceFeed priceFeed = PriceFeed.create();
        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");

        PriceFeed priceFeed = PriceFeed.createFromConfigFile("config/configPrice.cfg");
        priceFeed.setProductProvider(products);

        FixClientLogic fixServerLogic = new FixClientLogic();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(fixServerLogic);
        strategyRunner.setProductProvider(products);
        priceFeed.logon();

        fixServerLogic.setPriceProvider(strategyRunner);
        fixServerLogic.setProductProvider(products);

//        OrderConnection orderConnection = OrderConnection.create();
        String username = System.getProperty("user.name").replace(" ","");
        String userSpecificFIXFilename ="config/configOrder_"+username+".cfg";
        String configFIXFilename = FileTool.exists(userSpecificFIXFilename)
                ? userSpecificFIXFilename : "config/configOrder.cfg";
        OrderConnection orderConnection = OrderConnection.createFromConfigFile(configFIXFilename);
        orderConnection.setProductProvider(products);
        orderConnection.logon();

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
        fixServerLogic.setOrderSender(strategyRunner);

        /*
        config/FIXServer.properties needs to provide the external account number of the user in the form:
        externalAccount=1234
         */
        PropertiesReader config = PropertiesReader.createFromConfigFile("config/FIXServer.properties");
//        PropertiesReader config = PropertiesReader.create();
        fixServerLogic.setConfig(config);

        Thread.sleep(2000);

        fixServerLogic.init();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        fixServerLogic.shutdown();
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
