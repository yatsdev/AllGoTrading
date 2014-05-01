package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
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
    public void subscribe(Product p, IConsumeMarketData consumer) {
        setMarketDataConsumer(consumer);
        SubscriptionMsg m = SubscriptionMsg.createFromProduct(p);
        senderSubscription.publish(Config.TOPIC_FOR_SUBSCRIPTIONS_DEFAULT, m);
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
        senderSubscription = new Sender<SubscriptionMsg>(Config.EXCHANGE_NAME_FOR_SUBSCRIPTIONS_DEFAULT, Config.SERVER_IP_DEFAULT);
        senderOrderNew = new Sender<OrderNewMsg>(Config.EXCHANGE_NAME_FOR_ORDERNEW_DEFAULT, Config.SERVER_IP_DEFAULT);
        senderOrderCancel = new Sender<OrderCancelMsg>(Config.EXCHANGE_NAME_FOR_ORDERCANCEL_DEFAULT, Config.SERVER_IP_DEFAULT);
        receiverMarketdata = new BufferingReceiver<MarketDataMsg>(
                MarketDataMsg.class,
                Config.EXCHANGE_NAME_FOR_MARKET_DATA_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
        receiverMarketdata.setObserver(this);
        receiverMarketdata.start();

        receiverReceipt = new BufferingReceiver<ReceiptMsg>(
                ReceiptMsg.class,
                Config.EXCHANGE_NAME_FOR_RECEIPTS_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
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
        @Override
        public void onMarketData(MarketData marketData) {
        }
    }

    private static class ReceiptConsumerDummy implements IConsumeReceipt  {
        @Override
        public void onReceipt(Receipt receipt) {
        }
    }

} // class
