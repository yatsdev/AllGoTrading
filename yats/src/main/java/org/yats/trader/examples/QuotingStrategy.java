package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.math.BigDecimal;


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

    public QuotingStrategy() {
        super();
        lastBidOrder = OrderNew.NULL;
        shuttingDown=false;
        previousMarketData = MarketData.NULL;
    }


    private void handleMarketDataBidSide(MarketData marketData) {

        if(isInMarketBidSide()) {
            boolean changedSinceLastTick = !marketData.isPriceAndSizeSame(previousMarketData);
            BigDecimal bidChange_temporary=lastBidOrder.getLimit().subtract(getNewBid(marketData));
           // System.out.println(bidChange_temporary+" temporary non absolute number"); uncomment to test
            BigDecimal bidChange=bidChange_temporary.abs();
           // System.out.println(bidChange+" definitive absolute number");  uncomment to test
           //attention to the two lines above. Had to create a BigDecimal object since abs() is non static.
            if(changedSinceLastTick && bidChange.compareTo(BigDecimal.valueOf(0.01))>0) {
                log.info("changed price since last order: " + marketData);
            }

            boolean bidChangedEnoughForOrderUpdate = bidChange.compareTo(BigDecimal.valueOf(0.02)) > 0;
            if(!bidChangedEnoughForOrderUpdate) return;

            if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
        }

        if(!isInMarketBidSide() && position.compareTo(BigDecimal.ONE)<0) {
            sendOrderBidSide(getNewBid(marketData));
        }
        previousMarketData=marketData;
    }

    private BigDecimal getNewBid(MarketData marketData) {
        return marketData.getBid().multiply(BigDecimal.valueOf(0.995)).min(marketData.getBid().subtract(BigDecimal.valueOf(0.05)));
    //be careful about the line above, anyway it should work as per http://www.tutorialspoint.com/java/math/bigdecimal_min.htm exemplum
    }

    private void sendOrderBidSide(BigDecimal bid)
    {
        lastBidOrder = OrderNew.create()
                .withProductId(tradeProductId)
                .withExternalAccount(getExternalAccount())
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.BID)
                .withLimit(bid)
                .withSize(BigDecimal.valueOf(0.01));
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


    private BigDecimal position;
    private boolean shuttingDown;

    private String tradeProductId;
    IProvideProperties config;
    private OrderNew lastBidOrder;
    private MarketData previousMarketData;
    private boolean receivedOrderReceiptBidSide;

} // class
