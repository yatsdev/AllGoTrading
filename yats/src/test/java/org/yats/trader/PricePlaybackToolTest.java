package org.yats.trader;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;
import org.yats.common.PropertiesReader;
import org.yats.common.Stopwatch;
import org.yats.trader.examples.strategies.PriceRecorder;
import org.yats.trader.examples.tools.PricePlaybackTool;
import org.yats.trading.*;

import java.lang.reflect.Method;

public class PricePlaybackToolTest {

    @Test(groups = { "integration"})
    public void canReplayPricesInSameOrderAsTheyArrive()
    {
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

    @Test(groups = { "integration" })
    public void canReplayPricesWithDifferentSpeedFactors()
    {
        checkArrivalDelaysWithSpeedFactor(1);
        checkArrivalDelaysWithSpeedFactor(3);
        checkArrivalDelaysWithSpeedFactor(7);
        checkArrivalDelaysWithSpeedFactor(0.3);
    }

    private void checkArrivalDelaysWithSpeedFactor(double speedIncreaseFactor) {
        prop.set(speedFactorKey,Decimal.fromDouble(speedIncreaseFactor));
        playback = new PricePlaybackTool(prop);

        playback.createOrderedPriceList();
        Stopwatch stopwatch = Stopwatch.start();

        PriceData d1 = playback.getPriceData();
        assert(d1.getLast().isEqualTo(Decimal.ONE));

        playback.nextPriceData();
        assert(playback.hasMorePriceData());
        PriceData d2 = playback.getPriceDataDelayed(d1);

        Duration expectedArrivalDurationD2 = Duration.millis(Math.round(((double) shortIntervalMillis) / speedIncreaseFactor)-5);
        Duration elapsedTimeD2 = stopwatch.getElapsedTime();
        assert (elapsedTimeD2.isLongerThan(expectedArrivalDurationD2));
        Assert.assertTrue(elapsedTimeD2.isShorterThan(expectedArrivalDurationD2.plus(acceptableDelayMillis)),
                "expected="+expectedArrivalDurationD2+" elapsed="+elapsedTimeD2);

        playback.nextPriceData();
        PriceData d3 = playback.getPriceDataDelayed(d2);
        Duration expectedArrivalDurationD3 = Duration.millis(Math.round(((double) longerIntervalMillis) / speedIncreaseFactor)-5);
        Duration elapsedTimeD3 = stopwatch.getElapsedTime();
        Assert.assertTrue(elapsedTimeD3.isLongerThan(expectedArrivalDurationD3),
                "expected="+expectedArrivalDurationD3+" elapsed="+elapsedTimeD3);
        assert (elapsedTimeD3.isShorterThan(expectedArrivalDurationD3.plus(acceptableDelayMillis)));
    }

    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setUp(Method method) {
        String testDir = method.getName();
        FileTool.deleteDirectory(testDir);
        FileTool.createDirectories(testDir);
        prop = new PropertiesReader();
        prop.set("strategyName", "testStrategy");
        prop.set("internalAccount", "test");
        prop.set("productIdList", TestPriceData.TEST_SAP_PID + "," + TestPriceData.TEST_IBM_PID);
        prop.set("baseLocation", testDir);
        prop.set(speedFactorKey, Decimal.fromDouble(1));
        prop.set("exchangePriceData", "exchangePriceData");
        prop.set("serverIP", "127.0.0.1");
        recorder = new PriceRecorder();
        recorder.setPriceProvider(new PriceProviderDummy());
        recorder.setConfig(prop);

        DateTime startTime = DateTime.parse("2014-01-01T15:30:00");
        recorder.init();
        PriceData sap1 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.ONE, startTime);
        recorder.onPriceDataForStrategy(sap1);
        PriceData sap2 = PriceData.createFromLastWithTime(TestPriceData.TEST_SAP_PID, Decimal.TWO, startTime.plusMillis(shortIntervalMillis));
        recorder.onPriceDataForStrategy(sap2);
        PriceData ibm1 = PriceData.createFromLastWithTime(TestPriceData.TEST_IBM_PID, Decimal.TEN, startTime.plusMillis(longerIntervalMillis));
        recorder.onPriceDataForStrategy(ibm1);

        playback = new PricePlaybackTool(prop);
    }

    @AfterMethod
    public void tearDown(Method method) {
        FileTool.deleteDirectory(method.getName());
    }

    private final static int shortIntervalMillis = 400;
    private final static int longerIntervalMillis = 600;
    private final static int acceptableDelayMillis = 40;
    private final static String speedFactorKey = "playbackSpeedFactor";

    private PropertiesReader prop;
    private PriceRecorder recorder;
    private PricePlaybackTool playback;

    private class PriceProviderDummy implements  IProvidePriceFeed {
        @Override
        public void subscribe(String productId, IConsumePriceData consumer) {

        }
    }

} // class
