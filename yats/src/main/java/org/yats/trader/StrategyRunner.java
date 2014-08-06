package org.yats.trader;

import org.yats.common.IProvideProperties;
import org.yats.common.UniqueId;
import org.yats.trading.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class StrategyRunner implements IConsumeReceipt, ISendOrder, IConsumeMarketData, IProvidePriceFeed, Runnable, ISendReports, IConsumeSettings {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);


    @Override
    public void subscribe(String productId, IConsumeMarketData consumer)
    {
//        Product p = productProvider.getProductForProductId(productId);
        priceFeed.subscribe(productId, this);
        addConsumerForProductId(productId, consumer);
    }

    private void addConsumerForProductId(String productId, IConsumeMarketData consumer)
    {
        ConcurrentHashMap<String, IConsumeMarketData> consumers = getConsumersOfProductId(productId);
        consumers.put(consumer.getConsumerId().toString(), consumer);
        mapProductIdToConsumers.put(productId, consumers);
    }

    @Override
    public void onMarketData(MarketData marketData)
    {
        marketDataMap.put(marketData.getProductId(), marketData);
        updatedProductQueue.add(marketData.getProductId());
    }

    @Override
    public UniqueId getConsumerId() {
        return consumerId;
    }

    @Override
    public void onReceipt(Receipt receipt) {
        log.info("Received: {}", receipt);
        fillReceiptWithOrderData(receipt);
        receiptQueue.add(receipt);
        updatedProductQueue.add(receipt.getProductId());
    }

    private void fillReceiptWithOrderData(Receipt receipt) {
        if(!orderMap.containsKey(receipt.getOrderId().toString())) {
            log.error("received receipt for unknown order: {}", receipt);
            return;
        }
        String key = receipt.getOrderId().toString();
        OrderNew order = orderMap.get(key);
        receipt.setInternalAccount(order.getInternalAccount());
        if(receipt.isEndState()) orderMap.remove(key);
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

    @Override
    public void sendReports(IProvideProperties p) {
        reportSender.sendReports(p);
    }

    @Override
    public void onSettings(IProvideProperties p) {
        log.info("Received settings: {}", p);
        settingsQueue.add(p);
    }

    public boolean isProductSubscribed(String productId) {
        return mapProductIdToConsumers.containsKey(productId);
    }

    public void waitForProcessingQueues() {
        try {
            while(!updatedProductQueue.isEmpty()) Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage()); // todo: inherit class to throw
        }
    }

    public void stop()
    {
        shutdown=true;
        strategyThread.isAlive();
//        strategyThread.interrupt();
    }

    public void setOrderSender(ISendOrder orderSender) { this.orderSender = orderSender; }

    public void setReportSender(ISendReports reportSender) {
        this.reportSender = reportSender;
    }


    public void setPriceFeed(IProvidePriceFeed priceFeed) {
        this.priceFeed = priceFeed;
    }

    // todo: extend to support more than one trader in parallel
    public void addStrategy(StrategyBase strategy) {
        addReceiptConsumer(strategy);
        settingsConsumers.add(strategy);
//        addMarketDataConsumer(strategy);
    }

//    public void addMarketDataConsumer(IConsumeMarketData mdc) {
//        marketDataConsumers.add(mdc);
//    }

    public void addReceiptConsumer(IConsumeReceipt rc) {
        receiptConsumers.add(rc);
    }


    @Override
    public void run() {
        try {
            while (!shutdown) {
                String updatedProductId = updatedProductQueue.take();
                MarketData newData = marketDataMap.remove(updatedProductId);
                if(newData!=null) {
                    rateConverter.onMarketData(newData);
                    ConcurrentHashMap<String, IConsumeMarketData> marketDataConsumers = getConsumersOfProductId(newData.getProductId());
                    for(IConsumeMarketData md : marketDataConsumers.values()) {
                        md.onMarketData(newData);
                    }
                }
                 //todo: receipts should only be passed to the strategy that sent the corresponding order
                while(receiptQueue.size()>0){
                    Receipt r = receiptQueue.take();
                    for(IConsumeReceipt c : receiptConsumers) { c.onReceipt(r); }
                }
                while(settingsQueue.size()>0) {
                    for(IConsumeSettings c : settingsConsumers) { c.onSettings(settingsQueue.take()); }
                }
            }
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public void setRateConverter(RateConverter rateConverter) {
        this.rateConverter = rateConverter;

    }

    public StrategyRunner() {
        consumerId = UniqueId.create();
        priceFeed = new PriceFeedDummy();
        orderSender = new OrderSenderDummy();
        orderMap = new ConcurrentHashMap<String, OrderNew>();
        marketDataMap = new ConcurrentHashMap<String, MarketData>();
//        subscribedProducts = new ConcurrentHashMap<String, Product>();
        mapProductIdToConsumers = new ConcurrentHashMap<String, ConcurrentHashMap<String, IConsumeMarketData>>();
        updatedProductQueue = new LinkedBlockingQueue<String>();
        receiptQueue = new LinkedBlockingQueue<Receipt>();
        settingsQueue = new LinkedBlockingQueue<IProvideProperties>();
        strategyThread = new Thread(this);
        strategyThread.start();
        receiptConsumers = new LinkedList<IConsumeReceipt>();
        settingsConsumers = new LinkedList<IConsumeSettings>();
//        marketDataConsumers = new LinkedList<IConsumeMarketData>();
        shutdown = false;
        rateConverter = new RateConverter(new ProductList());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private ConcurrentHashMap<String, IConsumeMarketData> getConsumersOfProductId(String productId) {
        return mapProductIdToConsumers.containsKey(productId)
                ? mapProductIdToConsumers.get(productId)
                : new ConcurrentHashMap<String, IConsumeMarketData>();
    }

    private class PriceFeedDummy implements IProvidePriceFeed {
        @Override
        public void subscribe(String productId, IConsumeMarketData consumer) {
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


    private Thread strategyThread;
    private IProvidePriceFeed priceFeed;
    //    private ConcurrentHashMap<String, Product> subscribedProducts;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, IConsumeMarketData>> mapProductIdToConsumers;
    private ConcurrentHashMap<String, MarketData> marketDataMap;
    private ConcurrentHashMap<String, OrderNew> orderMap;
    private LinkedBlockingQueue<Receipt> receiptQueue;
    private LinkedBlockingQueue<IProvideProperties> settingsQueue;
    private LinkedBlockingQueue<String> updatedProductQueue;
    private ISendOrder orderSender;
    private ISendReports reportSender;
    private LinkedList<IConsumeReceipt> receiptConsumers;
    private LinkedList<IConsumeSettings> settingsConsumers;
    //    private LinkedList<IConsumeMarketData> marketDataConsumers;
    private IProvideProduct productProvider;
    private boolean shutdown;
    private UniqueId consumerId;
    private RateConverter rateConverter;

} // class
