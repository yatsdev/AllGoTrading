package org.yats.trader.examples.strategies;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PriceCheck extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceCheck.class);



    @Override
    public void onPriceData(PriceData priceData)
    {
        if(shuttingDown) return;
        if(!isInitialised()) return;

          for ( String key : lastPrices.keySet() ) {
          if(priceData.getProductId().compareTo(key)==0) {

            if(isFirstIteration==false) {
                boolean hugeLastChangeUp = priceData.getLast().isGreaterThan(lastPrices.get(key).getLast().multiply(upMove));
                boolean hugeBidChangeUp = priceData.getBid().isGreaterThan(lastPrices.get(key).getBid().multiply(upMove));
                boolean hugeAskChangeUp = priceData.getAsk().isGreaterThan(lastPrices.get(key).getAsk().multiply(upMove));
                boolean hugeLastChangeDown = priceData.getLast().isLessThan(lastPrices.get(key).getLast().multiply(downMove));
                boolean hugeBidChangeDown = priceData.getBid().isLessThan(lastPrices.get(key).getBid().multiply(downMove));
                boolean hugeAskChangeDown = priceData.getAsk().isLessThan(lastPrices.get(key).getAsk().multiply(downMove));
                boolean hugeChange = (hugeLastChangeUp || hugeBidChangeUp || hugeAskChangeUp
                        || hugeLastChangeDown || hugeBidChangeDown || hugeAskChangeDown);


                if (hugeChange) {
                    System.out.println("");
                    log.info("Huge change in price! " + priceData.toString() + " last:" + lastPrices.get(key).toString());
                } else {
                    dots++;
                    if (dots > 80) {
                        System.out.println("");
                        dots = 0;
                    }
                    System.out.print(".");
                }
            }
        lastPrices.put(key,priceData);
isFirstIteration=false;
         }
         }

    }


    @Override
    public void onReceipt(Receipt receipt)
    {
    }

    @Override
    public void onSettings(IProvideProperties p) {
        log.info(p.toString());
    }

    @Override
    public void init()
    {
        super.init();
        setInternalAccount(getConfig("internalAccount"));
        tradeProductIds = getConfig("tradeProductIds");
        String[] parts = tradeProductIds.split(",");
        tradeProductIdsNameList = Arrays.asList(parts);

        for(int i=0;i<tradeProductIdsNameList.size();i++){
            subscribe(tradeProductIdsNameList.get(i));
            lastPrices.put(tradeProductIdsNameList.get(i),PriceData.createFromLast(tradeProductIdsNameList.get(i),Decimal.ZERO));
        }

        upMove = Decimal.fromString(getConfig("upMove"));
        downMove = Decimal.fromString(getConfig("downMove"));

    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
    }

    public PriceCheck() {
        super();
        lastPrice = PriceData.NULL;
        shuttingDown=false;
    }

    private PriceData lastPrice;


    private ConcurrentHashMap<String,PriceData> lastPrices=new ConcurrentHashMap();
    private boolean shuttingDown;
    private String tradeProductIds;
    private static int dots = 0;
    private List<String> tradeProductIdsNameList;

    boolean isFirstIteration=true;
    private Decimal upMove;
    private Decimal downMove;
} // class
