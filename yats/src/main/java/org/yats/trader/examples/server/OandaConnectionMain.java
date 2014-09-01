package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.connectivity.oandarest.FXOrders;
import org.yats.connectivity.oandarest.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;
import org.yats.trading.ProductList;

import java.io.IOException;

/*
  Use template in
  config/OandaConnection_template.properties
  and create your personal config file.
 */

public class OandaConnectionMain implements IConsumePriceData {

    final Logger log = LoggerFactory.getLogger(FixClientMain.class);

    public void go() throws InterruptedException, IOException
    {
        String configFilename = Tool.getPersonalConfigFilename("config/OandaConnection");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        PriceFeed oandaFeed = PriceFeed.createFromPropertiesReader(prop);
        oandaFeed.setProductProvider(products);

        MarketToBusConnection marketToBusConnection = new MarketToBusConnection(prop);

        StrategyRunner strategyRunner = new StrategyRunner();
        strategyRunner.setPriceFeed(oandaFeed);
        strategyRunner.addStrategy(marketToBusConnection);
        strategyRunner.setProductProvider(products);
        oandaFeed.logon();

        marketToBusConnection.setPriceProvider(strategyRunner);
        marketToBusConnection.setProductProvider(products);

        FXOrders orderConnection = new FXOrders(prop);

        strategyRunner.setOrderSender(orderConnection);
        orderConnection.setReceiptConsumer(strategyRunner);
        marketToBusConnection.setOrderSender(strategyRunner);
        orderConnection.logon();

        strategyRunner.subscribe("OANDA_EURUSD", this);
        Thread.sleep(2000);

        marketToBusConnection.init();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        marketToBusConnection.shutdown();
        oandaFeed.shutdown();
        Thread.sleep(1000);

        System.exit(0);
    }

    public OandaConnectionMain() {
    }

    public static void main(String args[]) throws Exception {
        OandaConnectionMain q = new OandaConnectionMain();

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
