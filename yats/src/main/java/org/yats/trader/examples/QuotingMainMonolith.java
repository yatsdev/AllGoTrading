package org.yats.trader.examples;

import org.yats.common.PropertiesReader;
import org.yats.connectivity.fix.OrderConnection;
import org.yats.connectivity.fix.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trading.ReceiptStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//        PriceFeed priceFeed = PriceFeed.create();
        PriceFeed priceFeed = PriceFeed.createFromConfigFile("config/configPrice.cfg");

        QuotingStrategy strategy = new QuotingStrategy();
        ReceiptStorage receiptStorage = new ReceiptStorage();

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceFeed);
        strategyRunner.addStrategy(strategy);
        strategyRunner.addReceiptConsumer(receiptStorage);
        priceFeed.logon();

        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(receiptStorage);
        strategy.setProfitProvider(receiptStorage);

//        OrderConnection orderConnection = OrderConnection.create();
        OrderConnection orderConnection = OrderConnection.createFromConfigFile("config/configOrder.cfg");
        orderConnection.logon();

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
        strategy.setOrderSender(strategyRunner);

        // QuotingMain.properties needs to provide the external account number of the user in the form "externalAccount=1234"
        PropertiesReader config = PropertiesReader.createFromConfigFile("config/QuotingMain.properties");
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

    public QuotingMainMonolith() {
    }

    public static void main(String args[]) throws Exception {
        QuotingMainMonolith q = new QuotingMainMonolith();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            System.exit(-1);
        }
        System.exit(0);

    }

} // class
