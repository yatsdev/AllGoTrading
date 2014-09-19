package org.yats.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.trading.PositionServer;
import org.yats.trading.ProductList;

public class StrategyFactory {


    public StrategyFactory(StrategyRunner strategyRunner, PositionServer positionServer, ProductList productList) {

        this.strategyRunner = strategyRunner;
        this.positionServer = positionServer;
        this.productList = productList;
    }

    public StrategyBase createStrategy(IProvideProperties prop) {
        String strategyClassName = prop.get("strategyClass","noStrategyClassDefinedInProperties");
        StrategyBase strategy = instantiateStrategy(strategyClassName);
        strategy.setPriceProvider(strategyRunner);
        strategy.setPositionProvider(positionServer);
//        strategy.setProfitProvider(positionServer);
        strategy.setProductProvider(productList);
        strategy.setOrderSender(strategyRunner);
        strategy.setReportSender(strategyRunner);
        strategy.setPropertiesSaver(strategyRunner);
        strategy.setConfig(prop);
        strategy.setTimedCallbackProvider(strategyRunner);
        return strategy;
    }

    final Logger log = LoggerFactory.getLogger(StrategyFactory.class);

    public IProvideProperties loadProperties(String strategyName) {
        String configFilename = getConfigName(strategyName);
        log.info("trying to read config file: "+configFilename);
        if(!FileTool.exists(configFilename)) log.info("file not found: '"+configFilename+"'");
        return PropertiesReader.createFromConfigFile(configFilename);
    }

    public String getConfigName(String strategyName) {
        return Tool.getPersonalSubdirConfigFilename("config", "strategy", strategyName);
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
