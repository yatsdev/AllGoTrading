package org.yats.trader.examples.strategies;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.IAmCalledBackInTime;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;
import org.yats.trading.StorePriceCSV;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PriceRecorder extends StrategyBase implements IAmCalledBackInTime {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceRecorder.class);

    @Override
    public void onTimerCallback() {
        System.out.println("called back at " + DateTime.now());
        addTimedCallback(3, this);
    }

    @Override
    public void onPriceDataForStrategy(PriceData priceData) {
        if (shuttingDown) return;
        if (!isInitialised()) return;
        hasPriceData = false;
        for(String tradeProductId : pidList) {
            if (priceData.hasProductId(tradeProductId)) {
                hasPriceData = true;
            }
        }
        if(!hasPriceData) return;

        log.info("Received price #" + counter + ":" + priceData.toString());
        priceStoreMap.get(priceData.getProductId()).store(priceData);
        counter++;

        setReport("lastPrice", priceData.toString());
        setReport("counter", Integer.toString(counter));
        sendReports();
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
        setInternalAccount(getConfig("internalAccount", getName()));
        tradeProductList = getConfig("tradeProductId");
        baseLocation = getConfig("baseLocation");
        String[] parts = tradeProductList.split(",");
        pidList = Arrays.asList(parts);
        priceStoreMap = new ConcurrentHashMap<String, StorePriceCSV>();
        for(String tradeProductId : pidList) {
            subscribe(tradeProductId);
            StorePriceCSV csvStore = new StorePriceCSV(baseLocation,tradeProductId);
            priceStoreMap.put(tradeProductId,csvStore);

        }
//        addTimedCallback(3, this);
    }

    @Override
    public void onShutdown() {
        shuttingDown = true;
    }

    public PriceRecorder() {
        super();
        shuttingDown = false;
        counter = 0;
    }


    private boolean shuttingDown;
    private String tradeProductList;
    private String baseLocation;
    private int counter;
    private ConcurrentHashMap<String,StorePriceCSV> priceStoreMap;
    private  List<String> pidList;
    private boolean hasPriceData;

} // class

