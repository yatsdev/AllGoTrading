package org.yats.trader.examples.server;

import org.yats.common.PropertiesReader;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.Config;
import org.yats.trader.StrategyRunner;
import org.yats.trader.examples.strategies.MarketFollow;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;

import java.io.IOException;

/*
    This is an example of connecting to the FIX server of AllGoTrading, receiving prices and keeping
    an order on bid side at fixed distance from best bid.
    In this example a message bus is used to connect to a server that takes care of the FIX connection.
    To run the example please install and start the RabbitMQ server you can download from rabbitmq.com and start the
    FIX server connection FixClientMain. This server will connect to the FIX server and provide a
    message bus interface this example strategy will use.
    For an example with the FIX connectivity within the same binary have a look at QuotingMainMonolith.java
    The message bus allows to decentralize different services like connectivity, persistence of orders and receipts,
    running of strategies as well as visualisation and control tools.
 */

public class QuotingMain {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory

//    final Logger log = LoggerFactory.getLogger(QuotingMain.class);


    public void go() throws InterruptedException, IOException
    {

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        StrategyToBusConnection priceAndOrderConnection = new StrategyToBusConnection(Config.createRealProperties());

        MarketFollow strategy = new MarketFollow();
        PositionServer positionServer = new PositionServer();
        PositionServerLogic positionServerLogic = new PositionServerLogic(Config.createRealProperties());
        positionServerLogic.setPositionServer(positionServer);
        positionServerLogic.startSnapshotListener();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceAndOrderConnection);
        strategyRunner.addStrategy(strategy);
        strategyRunner.addReceiptConsumer(positionServer);
        strategyRunner.setProductProvider(products);

        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(positionServer);
//        strategy.setProfitProvider(positionServer);
        strategy.setProductProvider(products);


        strategyRunner.setOrderSender(priceAndOrderConnection);
        priceAndOrderConnection.setReceiptConsumer(strategyRunner);
        strategy.setOrderSender(strategyRunner);

        /*
        config/MarketFollow.properties needs to provide settings for the strategy.

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

        positionServerLogic.requestPositionSnapshotFromPositionServer();

        Thread.sleep(2000);

        strategy.init();

        System.out.println("\n===");
        System.out.println("Initialisation done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.out.print(System.in.read());
        System.out.println("\nexiting...\n");

        strategy.shutdown();

        Thread.sleep(1000);

        strategyRunner.stop();
        System.exit(0);

    }

    public QuotingMain() {
    }

    public static void main(String args[]) throws Exception {
        QuotingMain q = new QuotingMain();

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
