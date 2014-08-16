package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.MarketDataMsg;
import org.yats.trading.MarketData;
import org.yats.trading.ProductList;

public class PriceGeneratorLogic implements Runnable {

    final Logger log = LoggerFactory.getLogger(PriceGeneratorLogic.class);


    @Override
    public void run() {
        while(!shutdownThread) {
            Decimal change = (lastData.getLast().isGreaterThan(Decimal.ONE))
                    ? Decimal.CENT.multiply(Decimal.MINUSONE)
                    : Decimal.CENT;

            lastData = MarketData.createFromLast(pid, Decimal.ONE.add(change));
            MarketDataMsg m = MarketDataMsg.createFrom(lastData);

            senderMarketDataMsg.publish(m.getTopic(), m);
            log.info("Published: #"+counter+":"+lastData);
//            System.out.println("Published: #"+counter+":"+lastData);
            counter++;
            Tool.sleepFor(interval);
        }
    }

    public void go() {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderMarketDataMsg.close();
    }

    public PriceGeneratorLogic(IProvideProperties prop)
    {
        interval = prop.getAsDecimal("interval").toInt();
        pid=prop.get("productId");

        Config config =  Config.fromProperties(prop);
        lastData = MarketData.createFromLast(pid, Decimal.ONE);
        productList = ProductList.createFromFile("config/CFDProductList.csv");
        senderMarketDataMsg = new Sender<MarketDataMsg>(config.getExchangeMarketData(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
        counter=0;
    }


    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<MarketDataMsg> senderMarketDataMsg;
    private boolean shutdownThread;
    private Thread thread;
    private ProductList productList;
    private String pid;
    private MarketData lastData;
    private int counter;
    private int interval;



} // class
