package org.yats.trader.examples.strategies;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.trader.StrategyBase;
import org.yats.trading.MarketData;
import org.yats.trading.Receipt;

public class PriceCheck extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(QuotingStrategy.class);

    private static int dots = 0;

    @Override
    public void onMarketData(MarketData marketData)
    {
        if(shuttingDown) return;
        if(!isInitialised()) return;
        if(!marketData.hasProductId(tradeProductId)) return;
        if(lastPrice.equals(MarketData.NULL)) {
            lastPrice = marketData;
            return;
        }

        boolean hugeLastChangeUp = marketData.getLast().isGreaterThan(lastPrice.getLast().multiply(Decimal.fromString("1.01")));
        boolean hugeBidChangeUp = marketData.getBid().isGreaterThan(lastPrice.getBid().multiply(Decimal.fromString("1.01")));
        boolean hugeAskChangeUp = marketData.getAsk().isGreaterThan(lastPrice.getAsk().multiply(Decimal.fromString("1.01")));
        boolean hugeLastChangeDown = marketData.getLast().isLessThan(lastPrice.getLast().multiply(Decimal.fromString("0.99")));
        boolean hugeBidChangeDown = marketData.getBid().isLessThan(lastPrice.getBid().multiply(Decimal.fromString("0.99")));
        boolean hugeAskChangeDown = marketData.getAsk().isLessThan(lastPrice.getAsk().multiply(Decimal.fromString("0.99")));
        boolean hugeChange =  (hugeLastChangeUp || hugeBidChangeUp || hugeAskChangeUp
                || hugeLastChangeDown || hugeBidChangeDown || hugeAskChangeDown);

        if(hugeChange) {
            System.out.println("");
            log.info("Huge change in price! " + marketData.toString() + " last:" + lastPrice);
        } else {
            dots++;
            if(dots>80) {
                System.out.println("");
                dots=0;
            }
            System.out.print(".");
        }

        lastPrice = marketData;
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
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

    public PriceCheck() {
        super();
        lastPrice = MarketData.NULL;
        shuttingDown=false;
    }

    private MarketData lastPrice;


    private boolean shuttingDown;
    private String tradeProductId;

} // class
