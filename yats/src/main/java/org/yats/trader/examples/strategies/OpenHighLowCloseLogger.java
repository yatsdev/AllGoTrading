package org.yats.trader.examples.strategies;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.IAmCalledTimed;
import org.yats.trading.OpenHighLowClose;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

/**
 * Created by abbanerjee on 22/10/14.
 */
public class OpenHighLowCloseLogger extends StrategyBase implements IAmCalledTimed {
    final Logger log = LoggerFactory.getLogger(OpenHighLowCloseLogger.class);

    @Override
    public void onTimerCallback() {
        System.out.println("called back at "+ DateTime.now());
        ohlc.setComplete(true);
        log.info("OHLC #"+ ohlc.toString());
        addTimedCallback(observeWindow, this);
    }

    @Override
    public void onPriceDataForStrategy(PriceData priceData)
    {
        if(shuttingDown) return;
        if(!isInitialised()) return;
        if(!priceData.hasProductId(tradeProductId)) return;

        ohlc.process(priceData);
        log.info("Received price #"+counter+":" + priceData);
        counter++;

        setReport("lastPrice", priceData.toString());
        setReport("counter", Integer.toString(counter));
        sendReports();

    }

    @Override
    public void onReceiptForStrategy(Receipt receipt)
    {
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
    public void onInitStrategy()
    {
        setInternalAccount(getConfig("internalAccount",getName()));
        tradeProductId = getConfig("tradeProductId");
        observeWindow = getConfigAsInt("observeWindow");
        ohlc = new OpenHighLowClose();
        subscribe(tradeProductId);
        addTimedCallback(observeWindow, this);
    }

    @Override
    public void onShutdown()
    {
        shuttingDown=true;
    }

    public OpenHighLowCloseLogger() {
        super();
        shuttingDown=false;
        counter=0;
    }



    private boolean shuttingDown;
    private String tradeProductId;
    private int observeWindow;
    private int counter;
    private OpenHighLowClose ohlc;
}
