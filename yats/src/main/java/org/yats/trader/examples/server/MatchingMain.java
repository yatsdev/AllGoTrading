package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.matching.InternalMarket;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.trader.StrategyRunner;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;
import org.yats.trading.ProductList;

import java.io.IOException;

/*
  Use template in
  config/MatchingMain_template.properties
  and create your personal config file.
 */

public class MatchingMain implements IConsumePriceData {

    final Logger log = LoggerFactory.getLogger(MatchingMain.class);

    public void go() throws InterruptedException, IOException
    {
        String configFilename = Tool.getPersonalConfigFilename("config/MatchingMain");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection(prop);

        InternalMarket internalMarket = new InternalMarket(prop.get("externalAccount"), prop.get("marketName"));
        internalMarket.setProductProvider(products);

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(internalMarket);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);

        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);


        strategyRunner.setOrderSender(internalMarket);
        internalMarket.setReceiptConsumer(strategyRunner);
        internalMarket.setPriceConsumer(strategyRunner);
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
//        oandaFeed.close();
        Thread.sleep(1000);

        System.exit(0);
    }

    public MatchingMain() {
    }

    public static void main(String args[]) throws Exception {
        MatchingMain q = new MatchingMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    @Override
    public void onPriceData(PriceData priceData) {
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

} // class
