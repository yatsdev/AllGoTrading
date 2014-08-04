package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.common.FileTool;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.fix.OrderConnection;
import org.yats.connectivity.fix.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trader.examples.strategies.MarketFollow;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;
import org.yats.trading.ReceiptStorageCSV;

import java.io.IOException;

/*
    An example of connecting to a FIX server, receiving prices and keeping an order on bid side at fixed distance
    from best bid. This example uses the FIX connection in the same executable.
 */

public class QuotingMainMonolith {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory

    final Logger log = LoggerFactory.getLogger(QuotingMainMonolith.class);


    public void go() throws InterruptedException, IOException
    {
        String configFIXOrderFilename = getPersonalConfigFilename("config/FIXOrder");
        String configFIXPriceFilename = getPersonalConfigFilename("config/FIXPrice");

//        PriceFeed priceFeed = PriceFeed.create();
        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed priceFeed = PriceFeed.createFromConfigFile(configFIXPriceFilename);
        priceFeed.setProductProvider(products);

        MarketFollow strategy = new MarketFollow();
        ReceiptStorageCSV receiptStorage = new ReceiptStorageCSV();
        PositionServer positionServer = new PositionServer();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(strategy);
        strategyRunner.addReceiptConsumer(receiptStorage);
        strategyRunner.addReceiptConsumer(positionServer);
        strategyRunner.setProductProvider(products);
        priceFeed.logon();

        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(positionServer);
//        strategy.setProfitProvider(positionServer);
        strategy.setProductProvider(products);

        OrderConnection orderConnection = OrderConnection.createFromConfigFile(configFIXOrderFilename);
        orderConnection.setProductProvider(products);
        orderConnection.logon();

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
        strategy.setOrderSender(strategyRunner);


        /*
        MarketFollow.properties needs to provide settings for the strategy.

        # Comments have a leading hash
        # your AllGoTrading account number
        externalAccount=1234

        # the id of the product you want to trade, e.g.:
        #SAP at xetra
        tradeProductId=4663789
        #IBM at nyse
        #tradeProductId = 4663747

        */

        PropertiesReader config = PropertiesReader.createFromConfigFile("config/MarketFollow.properties");
//        PropertiesReader config = PropertiesReader.create();
        strategy.setConfig(config);

        Thread.sleep(2000);

        strategy.init();

        System.out.println("\n===");
        System.out.println("Initialisation done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        strategy.shutdown();
        Thread.sleep(1000);

        System.exit(0);

//        final CountDownLatch shutdownLatch = new CountDownLatch(1);
//        shutdownLatch.await();

    }

    private String getPersonalConfigFilename(String prefix)
    {
        String username = System.getProperty("user.name").replace(" ","");
        String userSpecificFIXFilename = prefix+"_"+username+".properties";
        log.info("Trying to read config file: "+userSpecificFIXFilename);
        if(!FileTool.exists(userSpecificFIXFilename))
            throw new CommonExceptions.FileReadException(userSpecificFIXFilename+" not found!");
        return userSpecificFIXFilename;
    }


    public QuotingMainMonolith() {
    }

    public static void main(String args[]) throws Exception {
        QuotingMainMonolith q = new QuotingMainMonolith();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            System.out.println(r.getMessage());
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

} // class
