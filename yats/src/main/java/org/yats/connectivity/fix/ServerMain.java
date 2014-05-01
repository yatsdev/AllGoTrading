package org.yats.connectivity.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.trader.StrategyRunner;

import java.io.IOException;

/*
    This server provides a link between an external FIX server and the internal message bus. It passes
    subscriptions and orders from the message bus to the FIX server and prices and receipts
    back to the to the message bus.
 */

public class ServerMain {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory

    final Logger log = LoggerFactory.getLogger(ServerMain.class);


    public void go() throws InterruptedException, IOException
    {
//        PriceFeed priceFeed = PriceFeed.create();
        PriceFeed priceFeed = PriceFeed.createFromConfigFile("config/configPrice.cfg");

        ServerLogic strategy = new ServerLogic();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(strategy);
        priceFeed.logon();

        strategy.setPriceProvider(strategyRunner);

//        OrderConnection orderConnection = OrderConnection.create();
        OrderConnection orderConnection = OrderConnection.createFromConfigFile("config/configOrder.cfg");
        orderConnection.logon();

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
        strategy.setOrderSender(strategyRunner);

        // FIXServer.properties needs to provide the external account number of the user in the form "externalAccount=1234"
        PropertiesReader config = PropertiesReader.createFromConfigFile("config/FIXServer.properties");
//        PropertiesReader config = PropertiesReader.create();
        strategy.setConfig(config);

        Thread.sleep(2000);

        strategy.init();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        strategy.shutdown();
        Thread.sleep(1000);

        System.exit(0);
    }

    public ServerMain() {
    }

    public static void main(String args[]) throws Exception {
        ServerMain q = new ServerMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            System.exit(-1);
        }
        System.exit(0);

    }

} // class
