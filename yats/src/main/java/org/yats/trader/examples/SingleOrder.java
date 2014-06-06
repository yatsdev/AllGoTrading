package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.HashMap;

public class SingleOrder extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(QuotingStrategy.class);

    @Override
    public void onMarketData(MarketData marketData)
    {
        if(!marketData.hasProductId(tradeProductId)) return;
        if(!startPrice.equals(MarketData.NULL)) return;
        if(shuttingDown) return;
        startPrice = marketData;

        sendOrder(BookSide.ASK, marketData.getAsk().add(Decimal.ONE), Decimal.fromString("0.01"));
        sendOrder(BookSide.BID, marketData.getBid().subtract(Decimal.ONE), Decimal.fromString("0.02"));
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
        if(shuttingDown) return;
        if(receipt.getRejectReason().length()>0) {
            log.error("Received rejection! Stopping for now!");
            shutdown();
            System.exit(-1);
        }
        if(!receipt.hasProductId(tradeProductId)){
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }

        position = receipt.getPositionChange().add(position);

        log.debug("Received receipt: " + receipt);

        if(receipt.isEndState()) {
            orders.remove(receipt.getOrderId().toString()); ;
        }

    }

    @Override
    public void init()
    {
        setInternalAccount("quoting1");
        tradeProductId = getConfig("tradeProductId");
        subscribe(tradeProductId);
        position = getPositionForProduct(tradeProductId);
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        cancelOrders();
    }


    private void sendOrder(BookSide side, Decimal bid, Decimal orderSize)
    {
        OrderNew order = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(side)
                .withLimit(bid)
                .withSize(orderSize);
//        receivedOrderReceiptBidSide = false;
        orders.put(order.getOrderId().toString(), order);
        sendNewOrder(order);
    }

    private void cancelOrders() {
        for(OrderNew order : orders.values()) {
            OrderCancel o = order.createCancelOrder();
            sendOrderCancel(o);
        }
    }


    public SingleOrder() {
        super();
        startPrice = MarketData.NULL;
        shuttingDown=false;
        position = Decimal.ZERO;
        orders = new HashMap<String, OrderNew>();
    }

    private MarketData startPrice;


    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private HashMap<String, OrderNew> orders;


} // class
