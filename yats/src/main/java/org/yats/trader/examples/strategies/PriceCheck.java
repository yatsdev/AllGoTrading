package org.yats.trader.examples.strategies;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.PriceData;
import org.yats.trading.Receipt;

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
        if(!priceData.hasProductId(tradeProductId)) return;
        if(lastPrice.equals(PriceData.NULL)) {
            lastPrice = priceData;
            return;
        }

        boolean hugeLastChangeUp = priceData.getLast().isGreaterThan(lastPrice.getLast().multiply(Decimal.fromString("1.01")));
        boolean hugeBidChangeUp = priceData.getBid().isGreaterThan(lastPrice.getBid().multiply(Decimal.fromString("1.01")));
        boolean hugeAskChangeUp = priceData.getAsk().isGreaterThan(lastPrice.getAsk().multiply(Decimal.fromString("1.01")));
        boolean hugeLastChangeDown = priceData.getLast().isLessThan(lastPrice.getLast().multiply(Decimal.fromString("0.99")));
        boolean hugeBidChangeDown = priceData.getBid().isLessThan(lastPrice.getBid().multiply(Decimal.fromString("0.99")));
        boolean hugeAskChangeDown = priceData.getAsk().isLessThan(lastPrice.getAsk().multiply(Decimal.fromString("0.99")));
        boolean hugeChange =  (hugeLastChangeUp || hugeBidChangeUp || hugeAskChangeUp
                || hugeLastChangeDown || hugeBidChangeDown || hugeAskChangeDown);

        if(hugeChange) {
            System.out.println("");
            log.info("Huge change in price! " + priceData.toString() + " last:" + lastPrice);
        } else {
            dots++;
            if(dots>80) {
                System.out.println("");
                dots=0;
            }
            System.out.print(".");
        }

        lastPrice = priceData;
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

    public PriceCheck() {
        super();
        lastPrice = PriceData.NULL;
        shuttingDown=false;
    }

    private PriceData lastPrice;


    private boolean shuttingDown;
    private String tradeProductId;
    private static int dots = 0;

} // class
