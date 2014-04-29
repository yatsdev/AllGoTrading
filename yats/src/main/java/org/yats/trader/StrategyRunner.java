package org.yats.trader;

import org.yats.trading.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class StrategyRunner implements IConsumeReceipt, ISendOrder, IConsumeMarketData, IProvidePriceFeed, Runnable {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);

    @Override
    public void subscribe(Product p, IConsumeMarketData consumer)
    {
        priceFeed.subscribe(p, this);
        subscribedProducts.put(p.getId(), p);
    }

    @Override
    public void onMarketData(MarketData marketData)
    {
        marketDataMap.put(marketData.getSecurityId(), marketData);
        updatedProductQueue.add(marketData.getSecurityId());
    }

    @Override
    public void onReceipt(Receipt receipt) {
//        log.info("received {}", receipt);
        fillReceiptWithOrderData(receipt);
        receiptQueue.add(receipt);
        updatedProductQueue.add(receipt.getSecurityId());
    }

    private void fillReceiptWithOrderData(Receipt receipt) {
        if(!orderMap.containsKey(receipt.getOrderId().toString())) {
            log.info("received receipt for unknown order: {}", receipt);
            return;
        }

        OrderNew order = orderMap.get(receipt.getOrderId().toString());
        receipt.setInternalAccount(order.getInternalAccount());
    }

    @Override
    public void sendOrderNew(OrderNew orderNew) {
        log.info("sending {}", orderNew);
        orderMap.put(orderNew.getOrderId().toString(), orderNew);
        orderSender.sendOrderNew(orderNew);
    }

    @Override
    public void sendOrderCancel(OrderCancel orderCancel) {
        log.info("sending {}", orderCancel);
        orderSender.sendOrderCancel(orderCancel);
    }

    public boolean isProductSubscribed(String productId) {
        return subscribedProducts.containsKey(productId);
    }

    public void waitForProcessingQueues() {
        try {
            while(!updatedProductQueue.isEmpty()) Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage()); // todo: inherit class to throw
        }
    }

    public void stop()
    {
        strategyThread.interrupt();
    }

    public void setOrderSender(ISendOrder orderSender) { this.orderSender = orderSender; }

    public void setPriceFeed(IProvidePriceFeed priceFeed) {
        this.priceFeed = priceFeed;
    }

    // todo: extend to support more than one trader in parallel
    public void addStrategy(IConsumeMarketDataAndReceipt strategy) {
        receiptConsumers.add(strategy);
        marketDataConsumers.add(strategy);
    }

    public void addMarketDataConsumer(IConsumeMarketData mdc) {
        marketDataConsumers.add(mdc);
    }

    public void addReceiptConsumer(IConsumeReceipt rc) {
        receiptConsumers.add(rc);
    }

    public StrategyRunner() {
        priceFeed = new PriceFeedDummy();
        orderSender = new OrderSenderDummy();
        orderMap = new ConcurrentHashMap<String, OrderNew>();
        marketDataMap = new ConcurrentHashMap<String, MarketData>();
        subscribedProducts = new ConcurrentHashMap<String, Product>();
        updatedProductQueue = new LinkedBlockingQueue<String>();
        receiptQueue = new LinkedBlockingQueue<Receipt>();
        strategyThread = new Thread(this);
        strategyThread.start();
        receiptConsumers = new LinkedList<IConsumeReceipt>();
        marketDataConsumers = new LinkedList<IConsumeMarketData>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String updatedProductId = updatedProductQueue.take();
                MarketData newData = marketDataMap.remove(updatedProductId);
                //todo: pass data only to strategies that subscribed for the product
                if(newData!=null) {
                    for(IConsumeMarketData md : marketDataConsumers) { md.onMarketData(newData); }
                }
                 //todo: receipts should only be passed to trader that sent the corresponding order
                while(receiptQueue.size()>0){
                    Receipt r = receiptQueue.take();
                    for(IConsumeReceipt c : receiptConsumers) { c.onReceipt(r); }
                }
            }
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
    }


    private Thread strategyThread;
    private IProvidePriceFeed priceFeed;
    private ConcurrentHashMap<String, Product> subscribedProducts;
    private ConcurrentHashMap<String, MarketData> marketDataMap;
    private ConcurrentHashMap<String, OrderNew> orderMap;
    private LinkedBlockingQueue<Receipt> receiptQueue;
    private LinkedBlockingQueue<String> updatedProductQueue;
    private ISendOrder orderSender;
    private LinkedList<IConsumeReceipt> receiptConsumers;
    private LinkedList<IConsumeMarketData> marketDataConsumers;

    private class StrategyDummy implements IConsumeMarketDataAndReceipt {
        @Override
        public void onMarketData(MarketData marketData) {
            throw new RuntimeException("StrategyDummy can not process market data.");
        }

        @Override
        public void onReceipt(Receipt receipt) {
            throw new RuntimeException("StrategyDummy can not process receipts.");
        }
    }

    private class PriceFeedDummy implements IProvidePriceFeed {
        @Override
        public void subscribe(Product p, IConsumeMarketData consumer) {
            throw new RuntimeException("PriceFeedDummy can not subscribe.");
        }
    }

    private class OrderSenderDummy implements ISendOrder {
        @Override
        public void sendOrderNew(OrderNew order) {
            throw new RuntimeException("Can not sendOrderNew");
        }

        @Override
        public void sendOrderCancel(OrderCancel orderCancel) {
            throw new RuntimeException("Can not sendOrderCancel");
        }
    }
} // class
