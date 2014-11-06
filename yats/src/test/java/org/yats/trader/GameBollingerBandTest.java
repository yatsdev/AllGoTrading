package org.yats.trader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trader.examples.server.PositionServerMain;
import org.yats.trading.*;

import java.awt.print.Book;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by abbanerjee on 05/11/14.
 */


@Test(groups = { "inMemory" })
public class GameBollingerBandTest {

    private static double bidPrice[] = { 1.00000, 2.00000, 3.37337,3.37345,3.37349,3.37351,3.37355,3.3736,3.3737,3.37378,3.37381,3.37384,3.37385,3.37391,3.37393,3.37399,3.37402,3.37403,3.37413,3.3742,3.37421,3.3743};
    private static double askPrice[] = { 1.00000, 2.00000, 3.37530 ,3.3735,3.37354,3.37356,3.3736,3.37365,3.37375,3.37383,3.37386,3.37389,3.3739,3.37396,3.37398,3.37404,3.37407,3.37408,3.37418,3.37425,3.37426,3.37435};
    private static double meanOfMeans =  3.37391;
    private static double stdDevOfMeans = 0.000264924;
    private static double calculatedUpperBound =  3.37444;
    private static double caluclatedLowerBound =  3.37338;
    private static String productId = "TEST_OANDA_USDPLN";
    final int _BollingerWindowSize = 20;

    @Test
    public void whenRequestedNextOrder_getNextOrder_producesOrder() {

        BollingerBandsForProduct bandsForProduct = new BollingerBandsForProduct(productId,_BollingerWindowSize);

        for(int iterator =0; iterator < priceList.size() ; iterator++){ // Feed 22 prices in a 20 circular buffer
            bandsForProduct.addPrice(priceList.get(iterator));
        }

        BollingerTrader trader = new BollingerTrader(bandsForProduct, BookSide.ASK);




    }


    @Test
    public void whenRequestedBands_getBands_matchesManualCalculation() {

        BollingerBandsForProduct bandsForProduct = new BollingerBandsForProduct(productId,_BollingerWindowSize);

        for(int iterator =0; iterator < priceList.size() ; iterator++){ // Feed 22 prices in a 20 circular buffer
            bandsForProduct.addPrice(priceList.get(iterator));
        }

        double mean = bandsForProduct.Test_getAverageBidAndAsk();
        double stdDev = bandsForProduct.Test_getStandardDeviationOfAverage();
        Decimal manuallyCalculatedStdDev = Decimal.fromDouble(stdDevOfMeans);
        Decimal calculatedStdDev = Decimal.fromDouble(stdDev).roundToDigits(9);
        Decimal meanRounded = Decimal.fromDouble(mean).roundToDigits(5);
        Decimal manuallyCalculatedMean = Decimal.fromDouble(meanOfMeans);


        assert (manuallyCalculatedMean.isEqualTo(meanRounded));
        assert( manuallyCalculatedStdDev.isEqualTo(calculatedStdDev));

    }




    @Test
    public void whenNewPriceArrives_addPrice_addsThenToCricularBuffer() {

        BollingerBandsForProduct bandsForProduct = new BollingerBandsForProduct(productId,_BollingerWindowSize);

        for(int iterator =0; iterator < priceList.size() ; iterator++){ // Feed 22 prices in a 20 circular buffer
            bandsForProduct.addPrice(priceList.get(iterator));
        }

        assert (bandsForProduct.getMovingWindowSize() == bandsForProduct.getPriceDataListSize());
        assert (bandsForProduct.getListIndexCount() >= bandsForProduct.getPriceDataListSize());

    }

    //////////////////////////////////////////////////////////////
    @BeforeMethod
    public void setUp() {

        priceList = new LinkedList<PriceData>();


        for (int i =0;i < 22;i++){
            PriceData p = new PriceData(DateTime.now(DateTimeZone.UTC), productId,
                                                    Decimal.fromDouble(bidPrice[i]),
                                                     Decimal.fromDouble(askPrice[i]),
                                                     Decimal.fromDouble(bidPrice[i]),
                                                     Decimal.ONE, Decimal.ONE,Decimal.ONE);
            priceList.add(p);
        }
    }

    private LinkedList<PriceData> priceList;


 }


