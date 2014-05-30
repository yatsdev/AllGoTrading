package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.UniqueId;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trading.*;

public class GenericConnection implements IProvidePriceFeed, ISendOrder, IAmCalledBack {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(GenericConnection.class);


    @Override
    public void subscribe(String productId, IConsumeMarketData consumer) {
        setMarketDataConsumer(consumer);
        SubscriptionMsg m = SubscriptionMsg.fromProductId(productId);
        Config config = Config.DEFAULT;
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
    public void onCallback() {
        sendAllReceivedMarketData();
        sendAllReceivedReceipts();
    }

    private void sendAllReceivedMarketData() {
        while(receiverMarketdata.hasMoreMessages()) {
            MarketData m = receiverMarketdata.get().toMarketData();
            marketDataConsumer.onMarketData(m);
        }
    }

    private void sendAllReceivedReceipts() {
        while(receiverReceipt.hasMoreMessages()) {
            Receipt r = receiverReceipt.get().toReceipt();
            receiptConsumer.onReceipt(r);
        }
    }

    public void setMarketDataConsumer(IConsumeMarketData marketDataConsumer) {
        this.marketDataConsumer = marketDataConsumer;
    }

    public void setReceiptConsumer(IConsumeReceipt receiptConsumer) {
        this.receiptConsumer = receiptConsumer;
    }

    public GenericConnection() {
        marketDataConsumer=new MarketDataConsumerDummy();
        receiptConsumer=new ReceiptConsumerDummy();
        Config config = Config.DEFAULT;
        senderSubscription = new Sender<SubscriptionMsg>(config.getExchangeSubscription(), config.getServerIP());
        senderOrderNew = new Sender<OrderNewMsg>(config.getExchangeOrderNew(), config.getServerIP());
        senderOrderCancel = new Sender<OrderCancelMsg>(config.getExchangeOrderCancel(), config.getServerIP());
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
    IConsumeMarketData marketDataConsumer;
    IConsumeReceipt receiptConsumer;
    BufferingReceiver<MarketDataMsg> receiverMarketdata;
    BufferingReceiver<ReceiptMsg> receiverReceipt;

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

} // class
