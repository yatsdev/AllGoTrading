package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.KeyValueMsg;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ReportGeneratorLogic implements Runnable {

    final Logger log = LoggerFactory.getLogger(ReportGeneratorLogic.class);


    @Override
    public void run() {
        Random rnd = new Random();
        while(!shutdownThread) {
            for(String name : strategyNameList) {
                IProvideProperties p = new PropertiesReader();
                p.set("strategyName",name);
                if(rnd.nextInt(10)>5) p.set("lastStep", Integer.toString(rnd.nextInt(100)));
                if(rnd.nextInt(10)>5) p.set("averageOrderSize", Integer.toString(rnd.nextInt(100)));
                if(rnd.nextInt(10)>5) p.set("maxUsedPositionSize", Integer.toString(rnd.nextInt(100)));
                if(rnd.nextInt(10)>5) p.set("orderRoundtripTime", Integer.toString(rnd.nextInt(100)));
                KeyValueMsg m = KeyValueMsg.fromProperties(p);
                senderReport.publish(m.getTopic(), m);
            }
            log.info("Published "+counter+" data sets of "+ strategyNameList.size()+" reports.");
            counter++;
            Tool.sleepFor(interval);
        }
    }

    public void go() {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderReport.close();
    }

    public ReportGeneratorLogic(IProvideProperties prop)
    {
        interval = prop.getAsDecimal("interval").toInt();

        String pidListString=prop.get("strategyNames");
        String[] parts = pidListString.split(",");
        strategyNameList = Arrays.asList(parts);
        Config config =  Config.fromProperties(prop);
        senderReport = new Sender<KeyValueMsg>(config.getExchangeKeyValueFromStrategy(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
        counter=0;
    }


    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<KeyValueMsg> senderReport;
    private boolean shutdownThread;
    private Thread thread;
    private List<String> strategyNameList;
    private int counter;
    private int interval;




}
