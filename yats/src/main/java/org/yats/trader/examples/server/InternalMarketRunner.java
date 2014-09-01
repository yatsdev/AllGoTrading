package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.common.UniqueId;
import org.yats.connectivity.matching.InternalMarket;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trading.*;

import java.util.concurrent.LinkedBlockingQueue;

public class InternalMarketRunner implements IAmCalledBack, IConsumePriceDataAndReceipt, Runnable {

    final Logger log = LoggerFactory.getLogger(InternalMarketRunner.class);

    @Override
    public void onCallback() {
        sendAllReceivedSubscriptionToMarket();
        sendAllReceivedOrderCancelToMarket();
        sendAllReceivedOrderNewToMarket();
    }

    @Override
    public void onPriceData(PriceData priceData) {
        if(shuttingDown) return;
        PriceDataMsg data = PriceDataMsg.createFrom(priceData);
        log.info("Published: "+ priceData);
        senderPriceDataMsg.publish(data.getTopic(), data);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    @Override
    public void onReceipt(Receipt receipt) {
        if(shuttingDown) return;
        ReceiptMsg m = ReceiptMsg.fromReceipt(receipt);
        log.info("Published: "+receipt);
        senderReceipt.publish(m.getTopic(), m);
    }

    @Override
    public void run() {
        try {
            while (!shuttingDown) {
                Thread.yield();
                updatedProductQueue.take();
                while(subscriptionQueue.size()>0) {
                    SubscriptionMsg m = subscriptionQueue.take();
                    log.info("processing Subscription: "+m);
                    market.subscribe(m.productId, this);
                }
                while(orderNewQueue.size()>0){
                    OrderNew o = orderNewQueue.take();
                    log.info("processing OrderNew: "+o);
                    market.sendOrderNew(o);
                    if(orderCancelQueue.size()>0) {
                        log.debug("orderCancelQueue.size()="+orderCancelQueue.size());
                        break;
                    }
                    Thread.yield();
                }
                while(orderCancelQueue.size()>0){
                    OrderCancel c = orderCancelQueue.take();
                    log.info("processing OrderCancel: "+c);
                    market.sendOrderCancel(c);
                    if(orderNewQueue.size()>0) {
                        log.debug("orderNewQueue.size()="+orderNewQueue.size());
                        break;
                    }
                    Thread.yield();
                }
            }
        }catch(InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } catch(Throwable t) {
            log.error(t.getMessage());
            t.printStackTrace();
        }
    }

    public void shutdown() {
        shuttingDown = true;
        receiverOrderCancel.close();
        receiverOrderNew.close();
        receiverSubscription.close();
        senderPriceDataMsg.close();
        senderReceipt.close();
    }

    public InternalMarketRunner(IProvideProperties prop) {
        productList = ProductList.createFromFile("config/CFDProductList.csv");

        Config config =  Config.fromProperties(prop);
        market = new InternalMarket(prop.get("externalAccount"), prop.get("marketName"));
        market.setPriceConsumer(this);
        market.setProductProvider(productList);
        market.setReceiptConsumer(this);

        shuttingDown = false;

        orderNewQueue = new LinkedBlockingQueue<OrderNew>();
        orderCancelQueue = new LinkedBlockingQueue<OrderCancel>();
        subscriptionQueue = new LinkedBlockingQueue<SubscriptionMsg>();
        updatedProductQueue = new LinkedBlockingQueue<String>();
        thread = new Thread(this);
        thread.start();


        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
        senderReceipt = new Sender<ReceiptMsg>(config.getExchangeReceipts(), config.getServerIP());

        receiverSubscription = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                config.getExchangeSubscription(),
                config.getTopicSubscriptions(),
                config.getServerIP());
        receiverSubscription.setObserver(this);
        receiverSubscription.start();

        receiverOrderNew = new BufferingReceiver<OrderNewMsg>(OrderNewMsg.class,
                config.getExchangeOrderNew(),
                "#",
                config.getServerIP());
        receiverOrderNew.setObserver(this);
        receiverOrderNew.start();

        receiverOrderCancel = new BufferingReceiver<OrderCancelMsg>(OrderCancelMsg.class,
                config.getExchangeOrderCancel(),
                "#",
                config.getServerIP());
        receiverOrderCancel.setObserver(this);
        receiverOrderCancel.start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private void sendAllReceivedSubscriptionToMarket() {
        while(receiverSubscription.hasMoreMessages()) {
            SubscriptionMsg m = receiverSubscription.get();
            if(!productList.containsProductWith(m.productId)) {
                log.debug("Attempt to subscribe for unknown product: "+m.productId);
                continue;
            }
            log.info("received Subscription for "+m.productId);
            subscriptionQueue.add(m);
            updatedProductQueue.add(m.productId);
        }
    }

    private void sendAllReceivedOrderCancelToMarket() {
        while(receiverOrderCancel.hasMoreMessages()) {
            OrderCancelMsg m = receiverOrderCancel.get();
            OrderCancel o = m.toOrderCancel();
            log.info("received OrderCancel: "+o);
            orderCancelQueue.add(o);
            updatedProductQueue.add(o.getProductId());
        }
    }

    private void sendAllReceivedOrderNewToMarket() {
        while(receiverOrderNew.hasMoreMessages()) {
            OrderNewMsg m = receiverOrderNew.get();
            OrderNew o = m.toOrderNew();
            log.info("received OrderNew: "+o);
            orderNewQueue.add(o);
            updatedProductQueue.add(o.getProductId());
        }
    }

    private Sender<PriceDataMsg> senderPriceDataMsg;
    private Sender<ReceiptMsg> senderReceipt;
    private BufferingReceiver<SubscriptionMsg> receiverSubscription;
    private BufferingReceiver<OrderNewMsg> receiverOrderNew;
    private BufferingReceiver<OrderCancelMsg> receiverOrderCancel;
    private boolean shuttingDown;
    private ProductList productList;
    private InternalMarket market;

    private LinkedBlockingQueue<SubscriptionMsg> subscriptionQueue;
    private LinkedBlockingQueue<OrderNew> orderNewQueue;
    private LinkedBlockingQueue<OrderCancel> orderCancelQueue;
    private LinkedBlockingQueue<String> updatedProductQueue;
    private Thread thread;


} // class
