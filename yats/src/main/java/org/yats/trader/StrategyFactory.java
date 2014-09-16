package org.yats.trader;

import org.yats.common.CommonExceptions;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;

public class StrategyFactory {


    public StrategyFactory(StrategyRunner strategyRunner, PositionServer positionServer, ProductList productList) {

        this.strategyRunner = strategyRunner;
        this.positionServer = positionServer;
        this.productList = productList;
    }

    public StrategyBase createStrategy(String strategyName) {

        String configFilename = Tool.getPersonalSubdirConfigFilename("config", "strategy", strategyName);
        PropertiesReader strategyConfig = PropertiesReader.createFromConfigFile(configFilename);
        String strategyClassName = strategyConfig.get("strategyClass");
        StrategyBase strategy = instantiateStrategy(strategyClassName);
        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(positionServer);
//        strategy.setProfitProvider(positionServer);
        strategy.setProductProvider(productList);
        strategy.setOrderSender(strategyRunner);
        strategy.setReportSender(strategyRunner);
        strategy.setName(strategyName);
        strategy.setConfig(strategyConfig);
        strategy.setTimedCallbackProvider(strategyRunner);
        return strategy;
    }


    /////////////////////////////////////////////////////////////////////////////////////


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



    private final StrategyRunner strategyRunner;
    private final PositionServer positionServer;
    private final ProductList productList;


} // class
