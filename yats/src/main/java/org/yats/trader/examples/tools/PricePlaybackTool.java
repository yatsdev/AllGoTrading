package org.yats.trader.examples.tools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PricePlaybackTool implements Runnable {

    public static void main(String args[]) throws Exception {
        {
            try {
                final String className = PricePlaybackTool.class.getSimpleName();
                String configFilename = Tool.getPersonalConfigFilename("config",className);
                PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
                PricePlaybackTool playback = new PricePlaybackTool(prop);

                PricePlaybackTool q = new PricePlaybackTool(prop);


                q.log.info("Starting "+className);

                Thread.sleep(1000);

                playback.go();

                System.out.println("\n===");
                System.out.println("Initialization done.");
                System.out.println("Press enter to exit.");
                System.out.println("===\n");
                System.in.read();
                System.out.println("\nexiting...\n");

                Thread.sleep(1000);

                playback.close();

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

    public boolean hasMorePriceData() {
        return priceDataIndex < orderedPriceList.size();
    }

    public PriceData getPriceData() {
        return orderedPriceList.get(priceDataIndex);
    }

    public void nextPriceData() {
        priceDataIndex++;
    }

    @Override
    public void run() {
        createOrderedPriceList();
        while(!shutdownThread) {
            priceDataIndex=0;
            PriceData previousPrice=null;
            while(hasMorePriceData()) {
                PriceData latestPrice = getPriceData();
                sleepBeforePublishingLatestPrice(previousPrice, latestPrice);
                publishPriceData(latestPrice);
                previousPrice=latestPrice;
                nextPriceData();
            }
        }
    }

    public void createOrderedPriceList() {
        while(true) {
            populateProductPriceMap();
            PriceData oldest = getOldestPriceFromProductPriceMap();
            if(oldest==null) break;
            productPriceMap.remove(oldest.getProductId());
            orderedPriceList.add(oldest);
        }
    }

    private PriceData getOldestPriceFromProductPriceMap() {
        PriceData found = null;
        for(PriceData p : productPriceMap.values()) {
            if(found==null) {found=p; continue; }
            if(found.getTimestamp().isAfter(p.getTimestamp())) found=p;
        }
        return found;
    }

    private void sleepBeforePublishingLatestPrice(PriceData prevPriceData, PriceData latestPriceData) {
        if(prevPriceData==null) return;
        Interval interval = new Interval(prevPriceData.getTimestamp(), latestPriceData.getTimestamp());
        Duration duration = interval.toDuration();
        double originalMillis = duration.getMillis();
        long sleepTimeMillis = (int)(originalMillis * speedFactor);
        Tool.sleepFor((int) sleepTimeMillis);
    }

    private void publishPriceData(PriceData latestPriceData) {
        PriceData newPriceData = new PriceData(DateTime.now(DateTimeZone.UTC),
                latestPriceData.getProductId(),latestPriceData.getBid(),latestPriceData.getAsk(),latestPriceData.getLast(),
                latestPriceData.getBidSize(), latestPriceData.getAskSize(),latestPriceData.getLastSize());

        PriceDataMsg m = PriceDataMsg.createFrom(newPriceData);
        senderPriceDataMsg.publish(m.getTopic(), m);
        log.info("Published["+productPublishedCount+"] " + latestPriceData.toString() );
    }

    private void populateProductPriceMap() {
        for (String inPid : pidList)
        {
            if(productPriceMap.containsKey(inPid)) continue;
            PriceData newData = readerMap.get(inPid).read();
            if(newData.equals(PriceData.NULL)) continue;
            productPriceMap.put(inPid, newData);
        }
    }

    public void go()
    {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderPriceDataMsg.close();
    }

    public PricePlaybackTool(IProvideProperties prop)
    {
        speedFactor = prop.getAsDecimal("playbackSpeedFactor").toDouble();
        baseLocation = prop.get("baseLocation");
        String pidListString =prop.get("productIdList");
        String[] parts = pidListString.split(",");
        pidList = Arrays.asList(parts);

        readerMap = new ConcurrentHashMap<String, ReadPriceCSV>();
        productPriceMap = new ConcurrentHashMap<String, PriceData>();

        for(String pid : pidList) {
            ReadPriceCSV csvReader = new ReadPriceCSV(baseLocation,pid);
            readerMap.put(pid,csvReader);
        }
        Config config =  Config.fromProperties(prop);
        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
        publishedOnce = false;
        productPublishedCount = 0;
        orderedPriceList = new ArrayList<PriceData>();
        priceDataIndex=0;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<PriceDataMsg> senderPriceDataMsg;
    private boolean shutdownThread,publishedOnce;
    private ConcurrentHashMap<String,PriceData> productPriceMap;
    private Thread thread;
    private List<String> pidList;
    private ConcurrentHashMap<String,ReadPriceCSV> readerMap;
    private double speedFactor;
    private String baseLocation;
    private int productPublishedCount;
    private List<PriceData> orderedPriceList;
    private int priceDataIndex;
}
