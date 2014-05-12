package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

/*
    This example keeps a bid five cents below the current best bid of the market. The order is canceled and
    a new order sent whenever the best bid of the market moved 2 cents or more.
*/

public class QuotingStrategy extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(QuotingStrategy.class);

    @Override
    public void onMarketData(MarketData marketData)
    {
        if(shuttingDown) return;
        if(!marketData.hasProductId(tradeProductId)) return;
        handleMarketDataBidSide(marketData);
    }

    @Override
    public void onReceipt(Receipt receipt)
    {
        if(shuttingDown) return;
        if(receipt.getRejectReason().length()>0) {
            log.error("Received rejection! Stopping for now!");
            System.exit(-1);
        }
        if(!receipt.hasProductId(tradeProductId)){
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }
        if(receipt.isForOrder(lastBidOrder)) {
            receivedOrderReceiptBidSide =true;
        }
        log.debug("Received receipt: " + receipt);
        position = receipt.getPositionChange().add(position);
    }

    @Override
    public void init()
    {
        setExternalAccount(config.get("externalAccount"));
        setInternalAccount("quoting1");
        tradeProductId = config.get("tradeProductId");
        subscribe(tradeProductId);
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
    }

    public void setConfig(IProvideProperties config) {
        this.config = config;
    }



    private void handleMarketDataBidSide(MarketData marketData) {

        if(isInMarketBidSide()) {
            boolean changedSinceLastTick = !marketData.isPriceAndSizeSame(previousMarketData);
            Decimal bidChange=lastBidOrder.getLimit().subtract(getNewBid(marketData)).abs();
            if(changedSinceLastTick && bidChange.isGreaterThan(Decimal.createFromDouble(0.01))) {
                log.info("changed price since last order: " + marketData);
            }

            boolean bidChangedEnoughForOrderUpdate = bidChange.isGreaterThan(Decimal.createFromDouble(0.02));
            if(!bidChangedEnoughForOrderUpdate) return;

            if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
        }

        boolean positionLessThanMaximum = position.isLessThan(Decimal.ONE);
        if(!isInMarketBidSide() && positionLessThanMaximum) {
            sendOrderBidSide(getNewBid(marketData));
        }
        previousMarketData=marketData;
    }

    private Decimal getNewBid(MarketData marketData) {
        return marketData.getBid().multiply(Decimal.createFromDouble(0.995))
                .min(marketData.getBid().subtract(Decimal.createFromDouble(0.05)));
    }

    private void sendOrderBidSide(Decimal bid)
    {
        lastBidOrder = OrderNew.create()
                .withProductId(tradeProductId)
                .withExternalAccount(getExternalAccount())
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.BID)
                .withLimit(bid)
                .withSize(Decimal.createFromDouble(0.01));
        receivedOrderReceiptBidSide = false;
        sendNewOrder(lastBidOrder);
    }

    private void cancelLastOrderBidSide() {
        OrderCancel o = lastBidOrder.createCancelOrder();
        sendOrderCancel(o);
        lastBidOrder=OrderNew.NULL;
        receivedOrderReceiptBidSide = false;
    }

    private boolean isInMarketBidSide() {
        return lastBidOrder != OrderNew.NULL;
    }


    public QuotingStrategy() {
        super();
        lastBidOrder = OrderNew.NULL;
        shuttingDown=false;
        previousMarketData = MarketData.NULL;
        position = Decimal.ZERO;
    }

    private Decimal position;
    private boolean shuttingDown;

    private String tradeProductId;
    IProvideProperties config;
    private OrderNew lastBidOrder;
    private MarketData previousMarketData;
    private boolean receivedOrderReceiptBidSide;

} // class
