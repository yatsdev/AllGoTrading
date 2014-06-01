package org.yats.trader.examples;

import org.yats.common.CommonExceptions;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.messagebus.GenericConnection;
import org.yats.messagebus.Config;
import org.yats.trader.StrategyBase;
import org.yats.trader.StrategyRunner;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;

import java.io.IOException;
import java.util.ArrayList;


         /*
         config/StrategyRunner.properties needs to contain a comma-separated list of strategy names like
         strategyNames=QuotingMain,Strategy2,Strategy3

        There need to exist files
        config/<StrategyName>.properties
        that provide settings for each of the strategies.

        For example:

        config/QuotingMain.properties:

        # Comments have a leading hash

        # qualified name of the strategy class:
        strategyClass=org.yats.trader.examples.QuotingStrategy

        # your AllGoTrading account number
        externalAccount=1234

        # the id of the product you want to trade, e.g.:
        #SAP at xetra
        tradeProductId=4663789
        #IBM at nyse
        #tradeProductId = 4663747

        */


public class StrategyRunnerMain {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory

//    final Logger log = LoggerFactory.getLogger(StrategyRunnerMain.class);


    public void go() throws InterruptedException, IOException
    {
        products = ProductList.createFromFile("config/CFDProductList.csv");
        GenericConnection priceAndOrderConnection = new GenericConnection();

        PropertiesReader strategyRunnerConfig = PropertiesReader.createFromConfigFile("config/StrategyRunner.properties");

        positionServer = new PositionServer();
        PositionServerLogic positionServerLogic = new PositionServerLogic(Config.DEFAULT);
        positionServerLogic.setPositionServer(positionServer);
        positionServerLogic.startSnapshotListener();

        strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(priceAndOrderConnection);
        strategyRunner.addReceiptConsumer(positionServer);
        strategyRunner.setProductProvider(products);
        strategyRunner.setOrderSender(priceAndOrderConnection);
        priceAndOrderConnection.setReceiptConsumer(strategyRunner);

        String strategyNamesString = strategyRunnerConfig.get("strategyNames");
        String[] strategyNames = strategyNamesString.split(",");

        ArrayList<StrategyBase> strategies = new ArrayList<StrategyBase>();
        for(String strategyName : strategyNames) {
            StrategyBase strategy = createStrategy(strategyName);
            strategyRunner.addStrategy(strategy);
            strategies.add(strategy);
        }

        positionServerLogic.requestPositionSnapshotFromPositionServer();

        Thread.sleep(2000);

        for(StrategyBase strategy : strategies) strategy.init();

        System.out.println("\n===");
        System.out.println("Initialisation done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.out.print(System.in.read());
        System.out.println("\nexiting...\n");

        for(StrategyBase strategy : strategies) strategy.shutdown();

        Thread.sleep(1000);

        strategyRunner.stop();
        System.exit(0);
    }

    private StrategyBase createStrategy(String strategyName) {
        PropertiesReader strategyConfig = PropertiesReader.createFromConfigFile("config/"+strategyName+".properties");
        String strategyClassName = strategyConfig.get("strategyClass");
        StrategyBase strategy = instantiateStrategy(strategyClassName);
        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(positionServer);
        strategy.setProfitProvider(positionServer);
        strategy.setProductProvider(products);
        strategy.setOrderSender(strategyRunner);
        strategy.setConfig(strategyConfig);
        return strategy;
    }

    private StrategyBase instantiateStrategy(String strategyName) {
        try {
            return (StrategyBase) Class.forName(strategyName).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new CommonExceptions.CouldNotInstantiateClassException("Class "+strategyName+" could not be created!");
    }

    public StrategyRunnerMain() {
    }

    public static void main(String args[]) throws Exception {
        StrategyRunnerMain q = new StrategyRunnerMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    private PositionServer positionServer;
    private StrategyRunner strategyRunner;
    private ProductList products;

} // class
