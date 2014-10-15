package org.yats.trader.examples.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PriceDataMsg;
import org.yats.trading.PriceData;
import org.yats.trading.ReadPriceCSV;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by abbanerjee on 15/10/14.
 * Plays back historical price recorder by PriceRecorder strategy
 */
public class PricePlaybackTool implements Runnable {

    public static void main(String args[]) throws Exception {
        {
            try {
                final String className = PricePlaybackTool.class.getSimpleName();
                String configFilename = Tool.getPersonalConfigFilename("config",className);
                PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
                PricePlaybackTool demo = new PricePlaybackTool(prop);

                PricePlaybackTool q = new PricePlaybackTool(prop);

                q.log.info("Starting "+className);

                Thread.sleep(1000);

                demo.go();

                System.out.println("\n===");
                System.out.println("Initialization done.");
                System.out.println("Press enter to exit.");
                System.out.println("===\n");
                System.in.read();
                System.out.println("\nexiting...\n");

                Thread.sleep(1000);

                demo.close();

                q.log.info("Done with "+className);
                System.exit(0);

            } catch (RuntimeException r)
            {
                r.printStackTrace();
                System.exit(-1);
            }
            System.exit(0);

        }
    }
    ///////////////////////////////////////////////////////////////////////////////


    final Logger log = LoggerFactory.getLogger(PriceGeneratorTool.class);


    @Override
    public void run() {
        while(!shutdownThread) {
            for (String pid : pidList) {
                //log.info("pid" + pid.toString());
                PriceData newData = readerMap.get(pid).read();
                //log.info("newData" + newData.toString());
                PriceDataMsg m = PriceDataMsg.createFrom(newData);
                senderPriceDataMsg.publish(m.getTopic(), m);
            }
            log.info("Published "+counter+" data sets of "+pidList.size()+" prices.");
            counter++;
            Tool.sleepFor(interval * speedFactor);
        }

    }

    public void go() {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderPriceDataMsg.close();
    }

    public PricePlaybackTool(IProvideProperties prop)
    {
        speedFactor = prop.getAsDecimal("playbackSpeedFactor").toInt();
        baseLocation = prop.get("baseLocation");
        interval = prop.getAsDecimal("interval").toInt();

        String pidListString =prop.get("productId");

        String[] parts = pidListString.split(",");
        pidList = Arrays.asList(parts);

        readerMap = new ConcurrentHashMap<String, ReadPriceCSV>();

        for(String pid : pidList) {
            ReadPriceCSV csvReader = new ReadPriceCSV(baseLocation,pid);
            readerMap.put(pid,csvReader);
        }
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
    private List<String> pidList;
    private ConcurrentHashMap<String,ReadPriceCSV> readerMap;
    private int counter;
    private int speedFactor;
    private String baseLocation;
    private int interval;
}
