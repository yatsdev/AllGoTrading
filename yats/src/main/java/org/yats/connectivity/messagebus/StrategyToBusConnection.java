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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class StrategyToBusConnection implements IProvidePriceFeed, ISendOrder, IAmCalledBack, ISendSettings, ISendReports {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyToBusConnection.class);


    @Override
    public void subscribe(String productId, IConsumePriceData consumer) {
        setPriceDataConsumer(consumer);
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
    public synchronized void onCallback() {
        if(!initDone) return;
        sendAllReceivedPriceData();
        sendAllReceivedReceipts();
        sendAllReceivedSettings();
        sendAllReceivedReports();
        sendAllReceivedPositionSnapshots();
    }

    private void sendAllReceivedPriceData() {
        while(receiverPriceData.hasMoreMessages()) {
            PriceDataMsg m = receiverPriceData.get();
            priceDataMap.put(m.productId, m);
        }
        for(PriceDataMsg m : priceDataMap.values()) {
            priceDataConsumer.onPriceData(m.toPriceData());
        }
        priceDataMap.clear();
    }

    private void sendAllReceivedReceipts() {
        while(receiverReceipt.hasMoreMessages()) {
            Receipt r = receiverReceipt.get().toReceipt();
//            log.debug("Received receipt (StrategyToBusConnection): "+r);
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
            KeyValueMsg m = receiverReports.get();
            String strategyName="unknown";
            IProvideProperties p = m.toProperties();
            if(p.exists("strategyName")) strategyName = p.get("strategyName");
            reportsMap.put(strategyName, p);
        }
        for(IProvideProperties p : reportsMap.values()) {
            reportsConsumer.onReport(p,receiverReports.hasMoreMessages());
        }
        reportsMap.clear();
    }

    private void sendAllReceivedPositionSnapshots() {
        while(receiverPositionSnapshot.hasMoreMessages()) {
            PositionSnapshotMsg m = receiverPositionSnapshot.get();
            if(receiverPositionSnapshot.hasMoreMessages()) continue;
            positionSnapshotConsumer.onPositionSnapshot(m.toPositionSnapshot());
        }
    }

    public void setPriceDataConsumer(IConsumePriceData priceDataConsumer) {
        this.priceDataConsumer = priceDataConsumer;
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

    public void setPositionSnapshotConsumer(IConsumePositionSnapshot positionSnapshotConsumer) {
        this.positionSnapshotConsumer = positionSnapshotConsumer;
    }

    public void close() {
        receiverPriceData.close();
        receiverReceipt.close();
        receiverPositionSnapshot.close();
        if(receiverReports!=null) receiverReports.close();
        if(receiverSettings!=null) receiverSettings.close();
        senderOrderNew.close();
        senderOrderCancel.close();
        senderReports.close();
        senderSettings.close();
        senderSubscription.close();
    }

    public StrategyToBusConnection(IProvideProperties p) {
        shuttingDown=false;
        initDone=false;

        priceDataMap = new ConcurrentHashMap<String, PriceDataMsg>();
        reportsMap = new ConcurrentHashMap<String, IProvideProperties>();
        orderNewQueue = new LinkedBlockingQueue<OrderNew>();
        orderCancelQueue = new LinkedBlockingQueue<OrderCancel>();
        subscriptionQueue = new LinkedBlockingQueue<SubscriptionMsg>();
        updatedProductQueue = new LinkedBlockingQueue<String>();


        priceDataConsumer =new PriceDataConsumerDummy();
        receiptConsumer=new ReceiptConsumerDummy();
        settingsConsumer =new SettingsConsumerDummy();
        reportsConsumer=new ReportsConsumerDummy();
        positionSnapshotConsumer = new PositionSnapshotConsumerDummy();
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

        receiverPositionSnapshot = new BufferingReceiver<PositionSnapshotMsg>(
                PositionSnapshotMsg.class,
                config.getExchangePositionSnapshot(),
                "#",
                config.getServerIP());
        receiverPositionSnapshot.setObserver(this);
        receiverPositionSnapshot.start();

        receiverPriceData = new BufferingReceiver<PriceDataMsg>(
                PriceDataMsg.class,
                config.getExchangePriceData(),
                "#",
                config.getServerIP());
        receiverPriceData.setObserver(this);
        receiverPriceData.start();

        receiverReceipt = new BufferingReceiver<ReceiptMsg>(
                ReceiptMsg.class,
                config.getExchangeReceipts(),
                "#",
                config.getServerIP());
        receiverReceipt.setObserver(this);
        receiverReceipt.start();
        initDone=true;
    }

    Sender<SubscriptionMsg> senderSubscription;
    Sender<OrderNewMsg> senderOrderNew;
    Sender<OrderCancelMsg> senderOrderCancel;
    Sender<KeyValueMsg> senderSettings;
    Sender<KeyValueMsg> senderReports;

    IConsumePriceData priceDataConsumer;
    IConsumeReceipt receiptConsumer;
    IConsumeSettings settingsConsumer;
    IConsumeReports reportsConsumer;
    IConsumePositionSnapshot positionSnapshotConsumer;

    BufferingReceiver<PriceDataMsg> receiverPriceData;
    ConcurrentHashMap<String, PriceDataMsg> priceDataMap;
    ConcurrentHashMap<String, IProvideProperties> reportsMap;
    BufferingReceiver<ReceiptMsg> receiverReceipt;
    BufferingReceiver<KeyValueMsg> receiverSettings;
    BufferingReceiver<KeyValueMsg> receiverReports;
    BufferingReceiver<PositionSnapshotMsg> receiverPositionSnapshot;
    Config config;
    boolean shuttingDown;
    boolean initDone;

    private LinkedBlockingQueue<SubscriptionMsg> subscriptionQueue;
    private LinkedBlockingQueue<OrderNew> orderNewQueue;
    private LinkedBlockingQueue<OrderCancel> orderCancelQueue;
    private LinkedBlockingQueue<String> updatedProductQueue;



    private static class PriceDataConsumerDummy implements IConsumePriceData {
        private PriceDataConsumerDummy() {
            id=new UniqueId();
        }

        @Override
        public void onPriceData(PriceData priceData) {
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
        public void onReport(IProvideProperties p, boolean hasMoreReports) {
        }
    }

    private static class PositionSnapshotConsumerDummy implements IConsumePositionSnapshot {
        @Override
        public void onPositionSnapshot(PositionSnapshot p) {
        }
    }


} // class
