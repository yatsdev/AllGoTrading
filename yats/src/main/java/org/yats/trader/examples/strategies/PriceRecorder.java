package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.IWritePrices;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

public class PriceRecorder extends StrategyBase  {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceRecorder.class);


    @Override
    public void onPriceDataForStrategy(PriceData priceData) {
    }


    @Override
    public void onReceiptForStrategy(Receipt receipt) {
    }

    @Override
    public void onStopStrategy() {
    }

    @Override
    public void onStartStrategy() {
    }

    @Override
    public void onSettingsForStrategy(IProvideProperties p) {
    }

    @Override
    public void onInitStrategy() {
    }

    @Override
    public void onShutdown() {
    }

    public PriceRecorder() {
    }

    public void setWriter(IWritePrices writer) {
        this.writer = writer;
    }

    private IWritePrices writer;
} // class

