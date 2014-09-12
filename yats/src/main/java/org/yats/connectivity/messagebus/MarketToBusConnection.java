package org.yats.connectivity.messagebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.*;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

public class MarketToBusConnection extends StrategyBase implements IAmCalledBack {


    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(MarketToBusConnection.class);

    @Override
    public void onPriceData(PriceData priceData)
    {
        if(shuttingDown) return;
        PriceDataMsg data = PriceDataMsg.createFrom(priceData);
        log.info("Published: "+ priceData);
        senderPriceDataMsg.publish(data.getTopic(), data);
    }

    @Override
    public void onReceipt(Receipt receipt)
    {
        if(shuttingDown) return;
        ReceiptMsg m = ReceiptMsg.fromReceipt(receipt);
        log.info("Published: "+receipt);
        senderReceipt.publish(m.getTopic(), m);
    }

    @Override
    public void onSettings(IProvideProperties p) {

    }

    @Override
    public void init()
    {
        setInternalAccount("marketToBusConnection");
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        receiverOrderCancel.close();
        receiverOrderNew.close();
        receiverSubscription.close();
        senderPriceDataMsg.close();
        senderReceipt.close();
    }

    public synchronized void onCallback() {
        sendAllReceivedSubscription();
        sendAllReceivedOrderNew();
        sendAllReceivedOrderCancel();
    }

    private void sendAllReceivedSubscription()  {
        while(receiverSubscription.hasMoreMessages()) {
            SubscriptionMsg m = receiverSubscription.get();
            try {
                Product p = getProductForProductId(m.productId);
                subscribe(p.getProductId());
            } catch(TradingExceptions.ItemNotFoundException e) {
                log.debug("Attempt to subscribe for unknown product: "+m.productId);
            }
        }
    }

    private void sendAllReceivedOrderNew() {
        while(receiverOrderNew.hasMoreMessages()) {
            OrderNewMsg m = receiverOrderNew.get();
            OrderNew o = m.toOrderNew();
            sendNewOrder(o);
            log.info("Sent "+o);
        }
    }

    private void sendAllReceivedOrderCancel() {
        while(receiverOrderCancel.hasMoreMessages()) {
            OrderCancelMsg m = receiverOrderCancel.get();
            OrderCancel o = m.toOrderCancel();
            sendOrderCancel(o);
            log.info("Sent "+o);
        }
    }

    public MarketToBusConnection(IProvideProperties prop) {
        super();
        shuttingDown=false;

        Config config =  Config.fromProperties(prop);
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

    Sender<PriceDataMsg> senderPriceDataMsg;
    Sender<ReceiptMsg> senderReceipt;
    BufferingReceiver<SubscriptionMsg> receiverSubscription;
    BufferingReceiver<OrderNewMsg> receiverOrderNew;
    BufferingReceiver<OrderCancelMsg> receiverOrderCancel;
    private boolean shuttingDown;

} // class
