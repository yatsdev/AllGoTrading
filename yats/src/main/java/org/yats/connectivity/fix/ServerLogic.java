package org.yats.connectivity.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.messagebus.*;
import org.yats.messagebus.messages.*;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

public class ServerLogic extends StrategyBase implements IAmCalledBack {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(ServerLogic.class);

    @Override
    public void onMarketData(MarketData marketData)
    {
        if(shuttingDown) return;
        MarketDataMsg data = MarketDataMsg.createFrom(marketData);
        log.info("Published: "+marketData);
        marketDataMsgSender.publish(data.getTopic(), data);
    }

    @Override
    public void onReceipt(Receipt receipt)
    {
        if(shuttingDown) return;
        ReceiptMsg m = ReceiptMsg.createFromReceipt(receipt);
        log.info("Published: "+receipt);
        receiptSender.publish(m.getTopic(), m);
    }

    @Override
    public void init()
    {
        setExternalAccount(config.get("externalAccount"));
        setInternalAccount("quoting1");
        marketDataMsgSender.init();
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
    }

    public void setConfig(IProvideProperties config) {
        this.config = config;
    }

    public void onCallback() {
        sendAllReceivedSubscription();
        sendAllReceivedOrderNew();
        sendAllReceivedOrderCancel();
    }

    private void sendAllReceivedSubscription() {
        while(receiverSubscription.hasMoreMessages()) {
            SubscriptionMsg m = receiverSubscription.get();
            Product p = getProductForProductId(m.productId);
            subscribe(p.getProductId());
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

    public ServerLogic() {
        super();
        shuttingDown=false;

        marketDataMsgSender = new Sender<MarketDataMsg>(Config.EXCHANGE_NAME_FOR_MARKET_DATA_DEFAULT, Config.SERVER_IP_DEFAULT);
        marketDataMsgSender.init();

        receiptSender = new Sender<ReceiptMsg>(Config.EXCHANGE_NAME_FOR_RECEIPTS_DEFAULT, Config.SERVER_IP_DEFAULT);
        receiptSender.init();

        receiverSubscription = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                Config.EXCHANGE_NAME_FOR_SUBSCRIPTIONS_DEFAULT,
                Config.TOPIC_FOR_SUBSCRIPTIONS_DEFAULT,
                Config.SERVER_IP_DEFAULT);
        receiverSubscription.setObserver(this);
        receiverSubscription.start();

        receiverOrderNew = new BufferingReceiver<OrderNewMsg>(OrderNewMsg.class,
                Config.EXCHANGE_NAME_FOR_ORDERNEW_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
        receiverOrderNew.setObserver(this);
        receiverOrderNew.start();

        receiverOrderCancel = new BufferingReceiver<OrderCancelMsg>(OrderCancelMsg.class,
                Config.EXCHANGE_NAME_FOR_ORDERCANCEL_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
        receiverOrderCancel.setObserver(this);
        receiverOrderCancel.start();

//        tradeProduct = new Product("4663789", "SAP", "XETR");
//        tradeProduct = new Product("4663745", "GE", "XNAS");
    }

//    private Product tradeProduct;

    Sender<MarketDataMsg> marketDataMsgSender;
    Sender<ReceiptMsg> receiptSender;
    BufferingReceiver<SubscriptionMsg> receiverSubscription;
    BufferingReceiver<OrderNewMsg> receiverOrderNew;
    BufferingReceiver<OrderCancelMsg> receiverOrderCancel;
    private boolean shuttingDown;
    IProvideProperties config;

} // class
