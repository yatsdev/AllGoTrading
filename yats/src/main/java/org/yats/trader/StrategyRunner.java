package org.yats.trader;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.yats.common.IProvideProperties;
import org.yats.common.UniqueId;
import org.yats.common.WaitingLinkedBlockingQueue;
import org.yats.trading.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class StrategyRunner implements IConsumeReceipt, ISendOrder,
        IConsumePriceData, IProvidePriceFeed, Runnable, ISendReports, IConsumeSettings,
        IProvideTimedCallback

{

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);


    @Override
    public void subscribe(String productId, IConsumePriceData consumer)
    {
//        Product p = productProvider.getProductWith(productId);
        priceFeed.subscribe(productId, this);
        addConsumerForProductId(productId, consumer);
    }

    private void addConsumerForProductId(String productId, IConsumePriceData consumer)
    {
        ConcurrentHashMap<String, IConsumePriceData> consumers = getConsumersOfProductId(productId);
        consumers.put(consumer.getConsumerId().toString(), consumer);
        mapProductIdToConsumers.put(productId, consumers);
    }

    @Override
    public void onPriceData(PriceData priceData)
    {
        priceDataMap.put(priceData.getProductId(), priceData);
        updatedProductQueue.add(priceData.getProductId());
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
            log.debug("received receipt for unknown order: {}", receipt);
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
            while(!updatedProductQueue.isEmpty()) Thread.sleep(200);
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
                callWaitingStrategies();

                String updatedProductId = updatedProductQueue.take();


                while(settingsQueue.size()>0) {
                    for(IConsumeSettings c : settingsConsumers) { c.onSettings(settingsQueue.take()); }
                }

                while(receiptQueue.size()>0){
                    Receipt r = receiptQueue.take();
                    for(IConsumeReceipt c : receiptConsumers) {
                        try {
                            c.onReceipt(r);
                        } catch (TradingExceptions.UnknownIdException e) {
                            log.error(e.getMessage());
                        }
                    }
                }

                PriceData newData = priceDataMap.remove(updatedProductId);
                if(newData!=null) {
                    rateConverter.onPriceData(newData);
                    ConcurrentHashMap<String, IConsumePriceData> priceDataConsumers = getConsumersOfProductId(newData.getProductId());
                    for(IConsumePriceData md : priceDataConsumers.values()) {
                        md.onPriceData(newData);
                    }
                }
            }
        }catch(InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void addTimedCallback(TimedCallback callback) {
        callbackList.add(callback);
    }

    private void callWaitingStrategies() throws InterruptedException {
        boolean itemArrived = false;
        while(!itemArrived) {
            Thread.yield();
            itemArrived = updatedProductQueue.isWaitedTillArrival(1, TimeUnit.SECONDS);

            DateTime now = DateTime.now();
            List<TimedCallback> temp = new ArrayList<TimedCallback>(callbackList);
            for (TimedCallback callback : temp)
            {
                if(callback.isTimeToCall(now)) {
                    callback.call();
                    callbackList.remove(callback);
                }
            }
        }
    }

    private ArrayList<TimedCallback> callbackList;

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
        callbackList =new ArrayList<TimedCallback>();
        orderMap = new ConcurrentHashMap<String, OrderNew>();
        priceDataMap = new ConcurrentHashMap<String, PriceData>();
//        subscribedProducts = new ConcurrentHashMap<String, Product>();
        mapProductIdToConsumers = new ConcurrentHashMap<String, ConcurrentHashMap<String, IConsumePriceData>>();
        updatedProductQueue = new WaitingLinkedBlockingQueue<String>();
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


    private ConcurrentHashMap<String, IConsumePriceData> getConsumersOfProductId(String productId) {
        return mapProductIdToConsumers.containsKey(productId)
                ? mapProductIdToConsumers.get(productId)
                : new ConcurrentHashMap<String, IConsumePriceData>();
    }

    private class PriceFeedDummy implements IProvidePriceFeed {
        @Override
        public void subscribe(String productId, IConsumePriceData consumer) {
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
    private ConcurrentHashMap<String, ConcurrentHashMap<String, IConsumePriceData>> mapProductIdToConsumers;
    private ConcurrentHashMap<String, PriceData> priceDataMap;
    private ConcurrentHashMap<String, OrderNew> orderMap;
    private LinkedBlockingQueue<Receipt> receiptQueue;
    private LinkedBlockingQueue<IProvideProperties> settingsQueue;
    private WaitingLinkedBlockingQueue<String> updatedProductQueue;
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
