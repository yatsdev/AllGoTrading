package org.yats.trader.examples.server;

import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trader.StrategyBase;
import org.yats.trader.StrategyFactory;
import org.yats.trader.StrategyRunner;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;
import org.yats.trading.RateConverter;

import java.io.IOException;

public class StrategyRunnerMain {

    //    final Logger log = LoggerFactory.getLogger(StrategyRunnerMain.class);

    public static void main(String args[]) throws Exception {
        StrategyRunnerMain q = new StrategyRunnerMain();

        try {
            q.createAllStrategies();
            q.requestSettings();
            q.waitForShutdown();
            q.shutdown();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }


    public StrategyRunnerMain() {

        productList = ProductList.createFromFile("config/CFDProductList.csv");
        rateConverter = new RateConverter(productList);
        final String className = StrategyRunnerMain.class.getSimpleName();
        String configFilename = Tool.getPersonalConfigFilename("config",className);
        strategyRunnerProperties = PropertiesReader.createFromConfigFile(configFilename);

        strategyToBusConnection = new StrategyToBusConnection(strategyRunnerProperties);
        positionServer = new PositionServer();
        positionServer.setRateConverter(rateConverter);
        positionServer.setProductList(productList);
        positionServerMain = new PositionServerMain(strategyRunnerProperties);
        positionServerMain.setPositionServer(positionServer);
        positionServerMain.startSnapshotListener();


        strategyRunner = new StrategyRunner();
        factory = new StrategyFactory(strategyRunner, positionServer, productList);
        strategyRunner.setFactory(factory);
        strategyRunner.setPriceFeed(strategyToBusConnection);
        strategyRunner.addReceiptConsumer(positionServer);
//        strategyRunner.setProductProvider(productList);
        strategyRunner.setOrderSender(strategyToBusConnection);
        strategyRunner.setReportSender(strategyToBusConnection);
        strategyRunner.setRateConverter(rateConverter);
        strategyToBusConnection.setReceiptConsumer(strategyRunner);
        strategyToBusConnection.setSettingsConsumer(strategyRunner);
        strategyToBusConnection.setPriceDataConsumer(strategyRunner);


        positionServerMain.requestPositionSnapshotFromPositionServer();
        Tool.sleepFor(500);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void createAllStrategies() throws InterruptedException, IOException
    {
        String strategyNamesString = strategyRunnerProperties.get("strategyNames","");
        String[] strategyNames = strategyNamesString.split(",");

        for(String strategyName : strategyNames) {
            if(strategyName.length()==0) continue;
            IProvideProperties prop = new PropertiesReader();
            prop.set(StrategyBase.SETTING_STRATEGYNAME, strategyName);
            strategyRunner.createNewStrategy(prop);
        }
    }

    private void requestSettings() {
        IProvideProperties p = new PropertiesReader();
        p.set("sendAllSettings","sendAllSettings");
        strategyToBusConnection.sendReports(p);
    }

    private void waitForShutdown() throws InterruptedException, IOException {
        System.out.println("\n===");
        System.out.println("Initialisation done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.out.print(System.in.read());
        System.out.println("\nexiting...\n");
    }

    private void shutdown() throws InterruptedException {
        strategyRunner.shutdownAllStrategies();

        Thread.sleep(1000);

        strategyRunner.stop();
        strategyToBusConnection.close();
        positionServerMain.close();

        System.exit(0);
    }


    private PositionServer positionServer;
    private StrategyRunner strategyRunner;
    private ProductList productList;
    private RateConverter rateConverter;
    private PositionServerMain positionServerMain;
    private PropertiesReader strategyRunnerProperties;
    private StrategyToBusConnection strategyToBusConnection;

    private StrategyFactory factory;


} // class
