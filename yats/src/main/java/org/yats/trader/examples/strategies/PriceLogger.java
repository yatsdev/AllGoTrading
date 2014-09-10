package org.yats.trader.examples.strategies;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

public class PriceLogger extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceLogger.class);

    @Override
    public void onPriceData(PriceData priceData)
    {
        if(shuttingDown) return;
        if(!isInitialised()) return;
        if(!priceData.hasProductId(tradeProductId)) return;

        log.info("Received price #"+counter+":" + priceData);
        counter++;
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
    }

    @Override
    public void onSettings(IProvideProperties p) {

    }

    @Override
    public void init()
    {
        super.init();
        setInternalAccount(getConfig("internalAccount"));
        tradeProductId = getConfig("tradeProductId");
        subscribe(tradeProductId);
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
    }

    public PriceLogger() {
        super();
        shuttingDown=false;
        counter=0;
    }



    private boolean shuttingDown;
    private String tradeProductId;
    private int counter;

} // class
