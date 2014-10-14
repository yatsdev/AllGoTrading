package org.yats.trader.examples.strategies;

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

public class MarketFollow extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(MarketFollow.class);

    @Override
    public void onPriceDataForStrategy(PriceData priceData)
    {
        if(shuttingDown) return;
        if(!priceData.hasProductId(tradeProductId)) return;
        handlePriceDataBidSide(priceData);
    }

    @Override
    public void onReceiptForStrategy(Receipt receipt)
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
        position = receipt.getPositionChangeOfBase().add(position);
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
        setInternalAccount(this.getClass().getSimpleName());
        tradeProductId = getConfig("tradeProductId");
        subscribe(tradeProductId);
    }

    @Override
    public void onShutdown()
    {
        shuttingDown=true;
        if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
    }

    private void handlePriceDataBidSide(PriceData priceData) {

        if(isInMarketBidSide()) {
            boolean changedSinceLastTick = !priceData.isPriceAndSizeSame(previousPriceData);
            Decimal lastBid = lastBidOrder.getLimit();
            Decimal bid = priceData.getBid();
            Decimal bidChange=lastBid.subtract(getNewBid(bid)).abs();
            if(changedSinceLastTick && bidChange.isGreaterThan(Decimal.fromDouble(0.01))) {
                log.info("changed price since last order: " + priceData);
            }

            boolean bidChangedEnoughForOrderUpdate = bidChange.isGreaterThan(Decimal.fromDouble(0.02));
            if(!bidChangedEnoughForOrderUpdate) return;

            if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
        }

        boolean positionLessThanMaximum = position.isLessThan(Decimal.ONE);
        if(!isInMarketBidSide() && positionLessThanMaximum) {
            Decimal bid = priceData.getBid();
            sendOrderBidSide(getNewBid(bid));
        }
        previousPriceData = priceData;
    }

    private Decimal getNewBid(Decimal oldBid) {
        Decimal bidRelative = oldBid.multiply(Decimal.fromDouble(0.995));
        Decimal bidAbsolute = oldBid.subtract(Decimal.fromDouble(0.05));
        return Decimal.min(bidRelative, bidAbsolute);
    }

    private void sendOrderBidSide(Decimal bid)
    {
        lastBidOrder = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.BID)
                .withLimit(bid)
                .withSize(Decimal.fromDouble(0.01));
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


    public MarketFollow() {
        super();
        lastBidOrder = OrderNew.NULL;
        shuttingDown=false;
        previousPriceData = PriceData.NULL;
        position = Decimal.ZERO;
    }

    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private OrderNew lastBidOrder;
    private PriceData previousPriceData;
    private boolean receivedOrderReceiptBidSide;

} // class
