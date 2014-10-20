package org.yats.trader;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;
import org.yats.common.PropertiesReader;
import org.yats.messagebus.Serializer;
import org.yats.messagebus.messages.PriceDataMsg;
import org.yats.trader.examples.strategies.PriceRecorder;
import org.yats.trader.examples.tools.PricePlaybackTool;
import org.yats.trading.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class PricePlaybackToolTest {

    @Test
    public void canReplayPricesInSameOrderAsTheyArrive()
    {
        DateTime time = DateTime.parse("2014-01-01T15:30:00");
        recorder.init();
        PriceData sap1 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.ONE, time);
        recorder.onPriceDataForStrategy(sap1);
        PriceData sap2 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.TWO, time.plusSeconds(5));
        recorder.onPriceDataForStrategy(sap2);
        PriceData ibm1 = PriceData.createFromLastWithTime(TestPriceData.TEST_IBM_PID, Decimal.TEN, time.plusSeconds(10));
        recorder.onPriceDataForStrategy(ibm1);

        playback.createOrderedPriceList();
        assert(playback.hasMorePriceData());
        PriceData d1 = playback.getPriceData();
        assert(d1.getLast().isEqualTo(Decimal.ONE));

        playback.nextPriceData();
        assert(playback.hasMorePriceData());
        PriceData d2 = playback.getPriceData();
        assert(d2.getLast().isEqualTo(Decimal.TWO));

        playback.nextPriceData();
        assert(playback.hasMorePriceData());
        PriceData d3 = playback.getPriceData();
        assert(d3.getLast().isEqualTo(Decimal.TEN));

        playback.nextPriceData();
        assert(!playback.hasMorePriceData());
    }

    @Test
    public void canReplayPricesWithSameSpeedAsTheyWereRecorded()
    {
    }

    @Test
    public void canReplayPricesWithFasterSpeedThanTheyWereRecorded()
    {
    }

    @BeforeMethod
    public void setUp() {
        FileTool.deleteDirectory(testDir);
        FileTool.createDirectories(testDir);
        PropertiesReader p = new PropertiesReader();
        p.set("strategyName","testStrategy");
        p.set("internalAccount","test");
        p.set("productIdList",TestPriceData.TEST_SAP_PID+","+TestPriceData.TEST_IBM_PID);
        p.set("baseLocation",testDir);
        p.set("playbackSpeedFactor",Decimal.fromDouble(0.1));
        p.set("exchangePriceData","exchangePriceData");
        p.set("serverIP","127.0.0.1");
        recorder = new PriceRecorder();
        recorder.setPriceProvider(new PriceProviderDummy());
        recorder.setConfig(p);

        playback = new PricePlaybackTool(p);

    }

    @AfterMethod
    public void tearDown() {
        FileTool.deleteDirectory(testDir);
    }

    String testDir = "PricePlaybackToolTest";
    private PriceRecorder recorder;
    private PricePlaybackTool playback;

    private class PriceProviderDummy implements  IProvidePriceFeed {
        @Override
        public void subscribe(String productId, IConsumePriceData consumer) {

        }
    }

//    private class MapWriter implements IWritePrices {
//
//        public ConcurrentHashMap<String, String> getRecordedItems() {
//            return recordedItems;
//        }
//
//        @Override
//        public void store(PriceData p) {
//            String last = recordedItems.contains(p.getProductId())
//                    ? recordedItems.get(p.getProductId())+"|"
//                    : "";
//            if(recordedItems.contains(p.getProductId())) last = recordedItems.get(p.getProductId());
//            PriceDataMsg m = PriceDataMsg.createFrom(p);
//            Serializer<PriceDataMsg> s = new Serializer<PriceDataMsg>();
//            String serial = s.convertToString(m);
//            String appended=last+serial;
//            recordedItems.put(p.getProductId(), appended);
//        }
//
//        private MapWriter() {
//            recordedItems = new ConcurrentHashMap<String, String>();
//        }
//
//        ConcurrentHashMap<String, String> recordedItems;
//    }
//
//    private class MapReader implements IReadPrices {
//
//        public void setRecordedItems(ConcurrentHashMap<String, String> recordedItems) {
//            this.recordedItems = recordedItems;
//        }
//
//        private MapReader() {
//            recordedItems = new ConcurrentHashMap<String, String>();
//        }
//
//        ConcurrentHashMap<String, String> recordedItems;
//    }

} // class
