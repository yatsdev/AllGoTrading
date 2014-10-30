package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.HashMap;

public class SingleOrder extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(MarketFollow.class);

    @Override
    public void onPriceDataForStrategy(PriceData priceData)
    {
        if(!priceData.hasProductId(tradeProductId)) return;
        if(!startPrice.equals(PriceData.NULL)) return;
        if(shuttingDown) return;
        startPrice = priceData;

        //sendOrder(BookSide.ASK, marketData.getAsk().add(Decimal.ONE), Decimal.fromString("0.01"));
        sendOrder(BookSide.BID, priceData.getBid().subtract(Decimal.fromString("0.001")), Decimal.fromString("1"));
    }


    @Override
    public void onReceiptForStrategy(Receipt receipt)
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

        position = receipt.getPositionChangeOfBase().add(position);

        log.debug("Received receipt: " + receipt);

        if(receipt.isEndState()) {
            orders.remove(receipt.getOrderId().toString());
        }

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
        position = getPositionForProduct(tradeProductId);
        startStrategy();
    }

    @Override
    public void onShutdown()
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
        startPrice = PriceData.NULL;
        shuttingDown=false;
        position = Decimal.ZERO;
        orders = new HashMap<String, OrderNew>();
    }

    private PriceData startPrice;


    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private HashMap<String, OrderNew> orders;


} // class
