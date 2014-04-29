package org.yats.trader.examples;

import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        handleMarketDataBidSide(marketData);
    }

    @Override
    public void onReceipt(Receipt receipt)
    {
        if(shuttingDown) return;
        if(receipt.getRejectReason().length()>0) {
            log.info("Received rejection! Stopping for now!");
            System.exit(-1);
        }
        if(!receipt.isForProduct(tradeProduct))                        {
            log.info("Received receipt for unknown product: "+receipt);
            return;
        }
        if(receipt.isForOrder(lastBidOrder)) {
            receivedOrderReceiptBidSide =true;
        }
        log.info("Received receipt: "+receipt);
        position += receipt.getPositionChange();
    }

    @Override
    public void init()
    {
        setExternalAccount(config.get("externalAccount"));
        setInternalAccount("quoting1");

        subscribe(tradeProduct);
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
//        tradeProduct = new Product("4663747", "IBM", "XNAS");
        tradeProduct = new Product("4663789", "SAP", "XETR");
        shuttingDown=false;
    }


    private void handleMarketDataBidSide(MarketData marketData) {

        double newBid = marketData.getBid() - 0.05;

        if(isInMarketBidSide()) {
            double bidChange = Math.abs(lastBidOrder.getLimit() - newBid);
            if(bidChange>0.001) {
                log.info("price: "+marketData);
            }

            boolean bidChangedEnoughForOrderUpdate = bidChange > 0.01;
            if(!bidChangedEnoughForOrderUpdate) return;

            if(isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
        }

        if(!isInMarketBidSide() && position<1) {
            sendOrderBidSide(newBid);
        }

    }

    private void sendOrderBidSide(double bid)
    {
        lastBidOrder = OrderNew.create()
                .withProduct(tradeProduct)
                .withExternalAccount(getExternalAccount())
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.BID)
                .withLimit(bid)
                .withSize(0.01);
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


    private double position;
    private boolean shuttingDown;

    private Product tradeProduct;
    IProvideProperties config;
    private OrderNew lastBidOrder;
    private boolean receivedOrderReceiptBidSide;

} // class
