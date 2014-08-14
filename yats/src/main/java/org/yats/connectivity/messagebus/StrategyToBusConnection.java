package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.common.UniqueId;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trading.*;

import java.util.concurrent.LinkedBlockingQueue;

public class StrategyToBusConnection implements Runnable, IProvidePriceFeed, ISendOrder, IAmCalledBack, ISendSettings, ISendReports {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyToBusConnection.class);


    @Override
    public void subscribe(String productId, IConsumeMarketData consumer) {
        setMarketDataConsumer(consumer);
        SubscriptionMsg m = SubscriptionMsg.fromProductId(productId);
//        Config config = Config.DEFAULT;
        senderSubscription.publish(config.getTopicSubscriptions(), m);
        log.debug("Published "+m);
    }

    @Override
    public void sendOrderNew(OrderNew orderNew) {
        OrderNewMsg m = OrderNewMsg.createFromOrderNew(orderNew);
        senderOrderNew.publish(m.getTopic(), m);
        log.debug("Published "+orderNew);
    }

    @Override
    public void sendOrderCancel(OrderCancel orderCancel) {
        OrderCancelMsg m = OrderCancelMsg.createFromOrderCancel(orderCancel);
        senderOrderCancel.publish(m.getTopic(),m);
        log.debug("Published "+orderCancel);
    }

    @Override
    public void sendSettings(IProvideProperties p) {
        KeyValueMsg m = KeyValueMsg.fromProperties(p);
        senderSettings.publish(m.getTopic(), m);
        log.debug("Published setting properties: "+p.getKeySet().size());
    }

    @Override
    public void sendReports(IProvideProperties p) {
        KeyValueMsg m = KeyValueMsg.fromProperties(p);
        senderReports.publish(m.getTopic(), m);
        log.debug("Published report properties: "+p.getKeySet().size());
    }

    @Override
    public void onCallback() {
        sendAllReceivedMarketData();
        sendAllReceivedReceipts();
        sendAllReceivedSettings();
        sendAllReceivedReports();
    }

    @Override
    public void run() {
//        try {
//            while (!shuttingDown) {
//                updatedProductQueue.take();
//                while(subscriptionQueue.size()>0) {
//                    SubscriptionMsg m = subscriptionQueue.take();
//                    log.info("processing Subscription: "+m);
//                    market.subscribe(m.productId, this);
//                }
//                while(orderCancelQueue.size()>0){
//                    OrderCancel c = orderCancelQueue.take();
//                    log.info("processing OrderCancel: "+c);
//                    market.sendOrderCancel(c);
//                }
//                while(orderNewQueue.size()>0){
//                    OrderNew o = orderNewQueue.take();
//                    log.info("processing OrderNew: "+o);
//                    market.sendOrderNew(o);
//                }
//            }
//        }catch(InterruptedException e) {
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
    }

    private void sendAllReceivedMarketData() {
        while(receiverMarketdata.hasMoreMessages()) {
            MarketData m = receiverMarketdata.get().toMarketData();
            log.info("STB: "+m);
            marketDataConsumer.onMarketData(m);
        }
    }

    private void sendAllReceivedReceipts() {
        while(receiverReceipt.hasMoreMessages()) {
            Receipt r = receiverReceipt.get().toReceipt();
            log.info("Received receipt (StrategyToBusConnection): "+r);
            receiptConsumer.onReceipt(r);
        }
    }

    private void sendAllReceivedSettings() {
        if(receiverSettings==null) return;
        while(receiverSettings.hasMoreMessages()) {
            IProvideProperties p = receiverSettings.get().toProperties();
            settingsConsumer.onSettings(p);
        }
    }

    private void sendAllReceivedReports() {
        if(receiverReports==null) return;
        while(receiverReports.hasMoreMessages()) {
            IProvideProperties p = receiverReports.get().toProperties();
            reportsConsumer.onReport(p);
        }
    }

    public void setMarketDataConsumer(IConsumeMarketData marketDataConsumer) {
        this.marketDataConsumer = marketDataConsumer;
    }

    public void setSettingsConsumer(IConsumeSettings settingsConsumer) {
        this.settingsConsumer = settingsConsumer;
    }

    public void setReportsConsumer(IConsumeReports reportsConsumer) {
        this.reportsConsumer = reportsConsumer;
    }

    public void setReceiptConsumer(IConsumeReceipt receiptConsumer) {
        this.receiptConsumer = receiptConsumer;
    }

    public StrategyToBusConnection(IProvideProperties p) {
        shuttingDown=false;

        orderNewQueue = new LinkedBlockingQueue<OrderNew>();
        orderCancelQueue = new LinkedBlockingQueue<OrderCancel>();
        subscriptionQueue = new LinkedBlockingQueue<SubscriptionMsg>();
        updatedProductQueue = new LinkedBlockingQueue<String>();
        thread = new Thread(this);
        thread.start();


        marketDataConsumer=new MarketDataConsumerDummy();
        receiptConsumer=new ReceiptConsumerDummy();
        settingsConsumer =new SettingsConsumerDummy();
        reportsConsumer=new ReportsConsumerDummy();
        config = Config.fromProperties(p);
        senderSubscription = new Sender<SubscriptionMsg>(config.getExchangeSubscription(), config.getServerIP());
        senderOrderNew = new Sender<OrderNewMsg>(config.getExchangeOrderNew(), config.getServerIP());
        senderOrderCancel = new Sender<OrderCancelMsg>(config.getExchangeOrderCancel(), config.getServerIP());
        senderSettings = new Sender<KeyValueMsg>(config.getExchangeKeyValueToStrategy(), config.getServerIP());
        senderReports = new Sender<KeyValueMsg>(config.getExchangeKeyValueFromStrategy(), config.getServerIP());

        receiverSettings=null;
        if(config.isReceiverForSettings()) {
            receiverSettings = new BufferingReceiver<KeyValueMsg>(
                    KeyValueMsg.class,
                    config.getExchangeKeyValueToStrategy(),
                    "#",
                    config.getServerIP());
            receiverSettings.setObserver(this);
            receiverSettings.start();
        }

        receiverReports=null;
        if(config.isReceiverForReports()) {
            receiverReports = new BufferingReceiver<KeyValueMsg>(
                    KeyValueMsg.class,
                    config.getExchangeKeyValueFromStrategy(),
                    "#",
                    config.getServerIP());
            receiverReports.setObserver(this);
            receiverReports.start();
        }

        receiverMarketdata = new BufferingReceiver<MarketDataMsg>(
                MarketDataMsg.class,
                config.getExchangeMarketData(),
                "#",
                config.getServerIP());
        receiverMarketdata.setObserver(this);
        receiverMarketdata.start();

        receiverReceipt = new BufferingReceiver<ReceiptMsg>(
                ReceiptMsg.class,
                config.getExchangeReceipts(),
                "#",
                config.getServerIP());
        receiverReceipt.setObserver(this);
        receiverReceipt.start();
    }

    Sender<SubscriptionMsg> senderSubscription;
    Sender<OrderNewMsg> senderOrderNew;
    Sender<OrderCancelMsg> senderOrderCancel;
    Sender<KeyValueMsg> senderSettings;
    Sender<KeyValueMsg> senderReports;

    IConsumeMarketData marketDataConsumer;
    IConsumeReceipt receiptConsumer;
    IConsumeSettings settingsConsumer;
    IConsumeReports reportsConsumer;
    BufferingReceiver<MarketDataMsg> receiverMarketdata;
    BufferingReceiver<ReceiptMsg> receiverReceipt;
    BufferingReceiver<KeyValueMsg> receiverSettings;
    BufferingReceiver<KeyValueMsg> receiverReports;
    Config config;
    boolean shuttingDown;

    private LinkedBlockingQueue<SubscriptionMsg> subscriptionQueue;
    private LinkedBlockingQueue<OrderNew> orderNewQueue;
    private LinkedBlockingQueue<OrderCancel> orderCancelQueue;
    private LinkedBlockingQueue<String> updatedProductQueue;
    private Thread thread;


    private static class MarketDataConsumerDummy implements IConsumeMarketData {
        private MarketDataConsumerDummy() {
            id=new UniqueId();
        }

        @Override
        public void onMarketData(MarketData marketData) {
        }

        @Override
        public UniqueId getConsumerId() {
            return id;
        }
        private UniqueId id;
    }

    private static class ReceiptConsumerDummy implements IConsumeReceipt  {
        @Override
        public void onReceipt(Receipt receipt) {
        }
    }

    private static class SettingsConsumerDummy implements IConsumeSettings {
        @Override
        public void onSettings(IProvideProperties p) {
        }
    }

    private static class ReportsConsumerDummy implements IConsumeReports {
        @Override
        public void onReport(IProvideProperties p) {
        }
    }


} // class
