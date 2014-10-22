package org.yats.trader.examples.strategies;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PriceRecorder extends StrategyBase implements IAmCalledTimed {

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
        tradeProductList = getConfig("productIdList");
        baseLocation = getConfig("baseLocation");
        String[] parts = tradeProductList.split(",");
        pidList = Arrays.asList(parts);
        priceStoreMap = new ConcurrentHashMap<String, StorePriceJson>();
        for(String tradeProductId : pidList) {
            subscribe(tradeProductId);
            StorePriceJson csvStore = new StorePriceJson(baseLocation,tradeProductId);
            priceStoreMap.put(tradeProductId,csvStore);

        }
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
    private ConcurrentHashMap<String,StorePriceJson> priceStoreMap;
    private  List<String> pidList;
    private boolean hasPriceData;

} // class

