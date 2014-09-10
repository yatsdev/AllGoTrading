package org.yats.trader.examples.tools;

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

public class ReportGeneratorTool implements Runnable {

    public static void main(String args[]) throws Exception {

        try {
            final String className = ReportGeneratorTool.class.getSimpleName();
            String configFilename = Tool.getPersonalConfigFilename("config",className);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
            ReportGeneratorTool logic = new ReportGeneratorTool(prop);
            logic.log.info("Starting "+className);

            Thread.sleep(1000);

            logic.go();

            System.out.println("\n===");
            System.out.println("Initialization done.");
            System.out.println("Press enter to exit.");
            System.out.println("===\n");
            System.in.read();
            System.out.println("\nexiting...\n");

            logic.close();

            Thread.sleep(2500);

            logic.log.info("Done with "+className);
            System.exit(0);
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final Logger log = LoggerFactory.getLogger(ReportGeneratorTool.class);


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

    public ReportGeneratorTool(IProvideProperties prop)
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
