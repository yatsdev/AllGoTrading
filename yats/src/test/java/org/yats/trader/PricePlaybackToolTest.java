package org.yats.trader;

import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.trader.examples.strategies.PriceRecorder;
import org.yats.trading.*;

import java.util.concurrent.ConcurrentHashMap;

public class PricePlaybackToolTest {

    @Test
    public void canReplayPricesInSameOrderAsTheyArrive()
    {
        DateTime time = DateTime.parse("2014-01-01T15:30:00");
        recorder.onInitStrategy();
        recorder.setWriter(writer);
        PriceData sap1 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.ONE, time);
        recorder.onPriceDataForStrategy(sap1);
        PriceData sap2 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.TWO, time.plusSeconds(5));
        recorder.onPriceDataForStrategy(sap2);
        PriceData ibm1 = PriceData.createFromLastWithTime(TestPriceData.TEST_IBM_PID, Decimal.TEN, time.plusSeconds(10));
        recorder.onPriceDataForStrategy(ibm1);
        ConcurrentHashMap<String, String> recordedItems = writer.getRecordedItems();
        playback.setReader(reader);
        reader.setRecordedItems(recordedItems);
        PriceData d1 = playback.getNext();
        assert(d1.getLast().equals(Decimal.ONE));
        PriceData d2 = playback.getNext();
        assert(d2.getLast().equals(Decimal.TWO));
        PriceData d3 = playback.getNext();
        assert(d3.getLast().equals(Decimal.TEN));
    }

    @BeforeMethod
    public void setUp() {
        recorder = new PriceRecorder();
        writer = new MapWriter();
        playback = new PricePlayback();
        reader = new MapReader();
    }

    private PriceRecorder recorder;
    private MapWriter writer;
    private MapReader reader;
    private PricePlayback playback;

    private class MapWriter implements IWritePrices {

        public ConcurrentHashMap<String, String> getRecordedItems() {
            return recordedItems;
        }


        private MapWriter() {
            recordedItems = new ConcurrentHashMap<String, String>();
        }

        ConcurrentHashMap<String, String> recordedItems;
    }

    private class MapReader implements IReadPrices {

        public void setRecordedItems(ConcurrentHashMap<String, String> recordedItems) {
            this.recordedItems = recordedItems;
        }

        private MapReader() {
            recordedItems = new ConcurrentHashMap<String, String>();
        }

        ConcurrentHashMap<String, String> recordedItems;
    }

} // class
