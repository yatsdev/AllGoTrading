package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PriceDataMsg;
import org.yats.trading.PriceData;
import org.yats.trading.ProductList;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PriceGeneratorLogic implements Runnable {

    final Logger log = LoggerFactory.getLogger(PriceGeneratorLogic.class);


    @Override
    public void run() {
        Random rnd = new Random();
        while(!shutdownThread) {
            for(String pid : pidList) {

                Decimal last = lastData.get(pid).getLast();
                Decimal lastRounded = last.roundToDigits(0);
                boolean aboveBasePrice = (last.isGreaterThan(lastRounded));
                Decimal change = aboveBasePrice
                        ? Decimal.CENT.multiply(Decimal.MINUSONE).multiply(Decimal.fromString("20"))
                        : Decimal.CENT;

                int bidDepth = 1+rnd.nextInt(10);
                int askDepth = 1+rnd.nextInt(10);
                PriceData newData = PriceData.createFromLastWithDepth(pid, last.add(change), bidDepth, askDepth, Decimal.CENT);
                lastData.put(pid, newData);
                PriceDataMsg m = PriceDataMsg.createFrom(newData);
                senderPriceDataMsg.publish(m.getTopic(), m);

//            System.out.println("Published: #"+counter+":"+lastData);
            }
            log.info("Published "+counter+" data sets of "+pidList.size()+" prices.");
            counter++;
            Tool.sleepFor(interval);
        }
    }

    public void go() {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderPriceDataMsg.close();
    }

    public PriceGeneratorLogic(IProvideProperties prop)
    {
        interval = prop.getAsDecimal("interval").toInt();

        String pidListString=prop.get("productId");
        String[] parts = pidListString.split(",");
        pidList = Arrays.asList(parts);
        lastData = new ConcurrentHashMap<String, PriceData>();
        int i=1;
        for(String pid : pidList) {
            Decimal priceBase = Decimal.fromDouble(i++);
            lastData.put(pid, PriceData.createFromLast(pid, priceBase));
        }

        productList = ProductList.createFromFile("config/CFDProductList.csv");
        Config config =  Config.fromProperties(prop);
        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
        counter=0;
    }


    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<PriceDataMsg> senderPriceDataMsg;
    private boolean shutdownThread;
    private Thread thread;
    private ProductList productList;
    private List<String> pidList;
    private ConcurrentHashMap<String,PriceData> lastData;
    private int counter;
    private int interval;



} // class
