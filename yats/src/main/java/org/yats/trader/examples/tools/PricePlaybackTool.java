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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

                if(productPublishedCount == 0 && publishedOnce == true){
                    for (String inPid : pidList)
                    {
                        PriceData newData = readerMap.get(inPid).read();
                        if(!newData.equals(PriceData.NULL))
                        {
                            lastReadProductPriceMap.put(inPid, newData);
                            DateTime startTime,endTime;
                            if(dataSetCounter == 0)
                            {
                                startTime = newData.getTimestamp();
                                endTime = currentTimeStamp;
                            }
                            else
                            {
                                startTime = currentTimeStamp;
                                endTime = newData.getTimestamp();

                            }
                            Interval interval = new Interval(startTime, endTime);
                            Duration duration = interval.toDuration();
                            //log.info("Deterministic Millis: " + duration.getMillis() + "for product" + newData.getProductId());
                            timeDistanceArray.add(BigInteger.valueOf(duration.getMillis()));
                        }

                    }
                    publishedOnce = false;

                    Collections.sort(timeDistanceArray);
                    if(!timeDistanceArray.isEmpty())
                    {
                        priceDistanceInMilliSeconds = timeDistanceArray.get(0).intValue();
                    }
                    else
                    {
                        //All prices consumed
                        shutdownThread = true;
                        System.exit(0);
                    }
                }

                PriceData latestPriceData = lastReadProductPriceMap.get(pid);
                DateTime lastPublishedPriceTimeStamp = latestPriceData.getTimestamp();
                DateTime startTime,endTime;

                if(dataSetCounter == 0){
                    startTime = lastPublishedPriceTimeStamp;
                    endTime = currentTimeStamp;
                }
                else{
                    startTime = currentTimeStamp;
                    endTime = lastPublishedPriceTimeStamp;

                }
                Interval interval = new Interval(startTime, endTime);
                Duration duration = interval.toDuration();
                long distanceFromLastPublished = duration.getMillis();

                distanceFromLastPublished = distanceFromLastPublished - priceDistanceInMilliSeconds;
                if(distanceFromLastPublished == 0 )
                {
                    PriceData newPriceData = new PriceData(DateTime.now(DateTimeZone.UTC),
                            pid,latestPriceData.getBid(),latestPriceData.getAsk(),latestPriceData.getLast(),
                            latestPriceData.getBidSize(), latestPriceData.getAskSize(),latestPriceData.getLastSize());

                    publishedOnce = true;
                    PriceDataMsg m = PriceDataMsg.createFrom(newPriceData);
                    senderPriceDataMsg.publish(m.getTopic(), m);
                    log.info("Published["+productPublishedCount+"] " + latestPriceData.toString() );
                    PriceData newData = readerMap.get(pid).read();
                    lastReadProductPriceMap.put(pid,newData);
                    long timeDelta = 0;
                    if(productPublishedCount > 0) {
                        timeDelta = priceDistanceInMilliSeconds - timeDistanceArray.get(productPublishedCount - 1).intValue();
                    }
                    else{
                        timeDelta = priceDistanceInMilliSeconds;
                        //Skip delay for the first time
                        timeDelta = dataSetCounter >0 ? priceDistanceInMilliSeconds:0;
                    }
                    long sleepTimeMilis = (int)(timeDelta * speedFactor);
                    log.info("SLEEP: " + sleepTimeMilis );
                    Tool.sleepFor((int) sleepTimeMilis);
                    if(productPublishedCount  == timeDistanceArray.size()-1)
                    {
                        productPublishedCount = 0;
                        currentTimeStamp = lastPublishedPriceTimeStamp;
                        dataSetCounter++;
                        timeDistanceArray.clear();
                        log.info("DataSet[" + dataSetCounter + "] published");
                    }
                    else{
                        priceDistanceInMilliSeconds = timeDistanceArray.get(++productPublishedCount).intValue();
                    }
                }
            }
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
        speedFactor = prop.getAsDecimal("playbackSpeedFactor").toDouble();
        baseLocation = prop.get("baseLocation");
        String pidListString =prop.get("productId");
        String[] parts = pidListString.split(",");
        pidList = Arrays.asList(parts);

        readerMap = new ConcurrentHashMap<String, ReadPriceCSV>();
        lastReadProductPriceMap = new ConcurrentHashMap<String, PriceData>();
        timeDistanceArray = new ArrayList<BigInteger>();

        for(String pid : pidList) {
            ReadPriceCSV csvReader = new ReadPriceCSV(baseLocation,pid);
            readerMap.put(pid,csvReader);
        }
        Config config =  Config.fromProperties(prop);
        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
        dataSetCounter=0;
        publishedOnce = false;
        currentTimeStamp = DateTime.now(DateTimeZone.UTC);
        //currentTimeStamp = DateTime.parse("2014-10-18T20:24:24.582Z");
        productPublishedCount = 0;

        for (String inPid : pidList) {
            PriceData newData = readerMap.get(inPid).read();
            lastReadProductPriceMap.put(inPid, newData);
            Interval interval = new Interval(newData.getTimestamp(), currentTimeStamp);
            Duration duration = interval.toDuration();
            //log.info("Deterministic Millis: " + duration.getMillis() + "for product" + newData.getProductId());
            timeDistanceArray.add(BigInteger.valueOf(duration.getMillis()));
        }

        Collections.sort(timeDistanceArray);
        if(!timeDistanceArray.isEmpty())
            priceDistanceInMilliSeconds = timeDistanceArray.get(0).intValue();


    }

    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<PriceDataMsg> senderPriceDataMsg;
    private boolean shutdownThread,publishedOnce;
    private ConcurrentHashMap<String,PriceData> lastReadProductPriceMap;
    private Thread thread;
    private List<String> pidList;
    private ConcurrentHashMap<String,ReadPriceCSV> readerMap;
    private int dataSetCounter;
    private double speedFactor;
    private String baseLocation;
    private ArrayList<BigInteger> timeDistanceArray;
    private DateTime currentTimeStamp;
    private long priceDistanceInMilliSeconds; //potential bug when recorded data is very old
    private int productPublishedCount;
}
