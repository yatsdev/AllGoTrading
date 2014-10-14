package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Statistics;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

/**
 * Created by macbook52 on 11/10/14.
 * Bollinger Bands consist of:
 * <p/>
 * an N-period moving average (MA)
 * an upper band at K times an N-period standard deviation above the moving average (MA + Kσ)
 * a lower band at K times an N-period standard deviation below the moving average (MA − Kσ)
 */
public class BollingerBands extends StrategyBase {
    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(BollingerBands.class);

    @Override
    public void onPriceDataForStrategy(PriceData priceData) {
        if (shuttingDown) return;
        if (!priceData.hasProductId(tradeProductId)) return;
        handlePriceDataBidSide(priceData);
    }

    @Override
    public void onReceiptForStrategy(Receipt receipt) {
        if (shuttingDown) return;
        if (receipt.getRejectReason().length() > 0) {
            log.error("Received rejection! Stopping for now!");
            System.exit(-1);
        }
        if (!receipt.hasProductId(tradeProductId)) {
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }
        if (receipt.isForOrder(lastBidOrder)) {
            receivedOrderReceiptBidSide = true;
        }
        log.debug("Received receipt: " + receipt);
        position = receipt.getPositionChangeOfBase().add(position);
    }

    @Override
    public void onStopStrategy() {
    }

    @Override
    public void onStartStrategy() {
    }

    @Override
    public void onSettingsForStrategy(IProvideProperties p) {
    }

    @Override
    public void onInitStrategy() {
        setInternalAccount(this.getClass().getSimpleName());
        tradeProductId = getConfig("tradeProductId");
        subscribe(tradeProductId);
    }

    @Override
    public void onShutdown() {
        shuttingDown = true;
        if (isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
    }

    private void handlePriceDataBidSide(PriceData priceData) {


        //
        Decimal bid = priceData.getBid();
        Decimal ask = priceData.getAsk();
        double midPrice = (bid.toDouble() + ask.toDouble()) / 2.0;

        if (dataPoints == -1) {
            oldBidPrice = bid.toDouble();
            oldAskPrice = ask.toDouble();
            oldMidPrice = midPrice;
            dataPoints++;
            log.info("[" + dataPoints + "] midPrice " + oldMidPrice);
        }

        if (oldMidPrice != midPrice) {


            double tradeProfit = 0.0;
            oldBidPrice = bid.toDouble();
            oldAskPrice = ask.toDouble();
            oldMidPrice = midPrice;

            _priceData[(int) dataPoints++ % _N_period] = midPrice;//bid.toDouble(); //circular buffer

            log.info("[" + dataPoints + "] midPrice " + midPrice);

            if (dataPoints > _N_period) {
                if (movingAverage >= bid.toDouble()) {
                         /* Signal to SELL a short position
                        Here
                        1. There is already an existing open long position and this short position
                           will simply close that long by selling at bid and initiate a new position
                           at bid
                        2. The trade went sour and the signal is to establish a new short position at possibly
                           a higher price than before. Hence book loss by covering the existing short position
                           at ask and establishing a new short position at ask.
                         */

                    //Short Trade
                    log.info("Short: at " + bid.toString());
                    if (openLong) {
                        if ((bid.toDouble() - longOpenPrice) > 0) {
                            openLong = false;
                            tradeProfit = bid.toDouble() - longOpenPrice;
                            cumProfit += tradeProfit;
                            log.info("(++) Closing previously open Long position at [longOpenPrice]: " + longOpenPrice);
                            log.info("(++) Closing previously open Long position at [bid]: " + bid.toDouble());
                            log.info("(++) Closing previously open Long position at [tradeProfit]: " + tradeProfit);
                            log.info("(++) Closing previously open Long position at [cumProfit]: " + cumProfit);
                        }

                    }
                        /*if (openShort) {
                            //coverShort (loss)
                            if((bid.toDouble() - shortOpenPrice ) > (ask.toDouble() - shortOpenPrice)) {//
                                tradeProfit = shortOpenPrice - ask.toDouble();
                                cumProfit += tradeProfit;
                                openShort = false;
                                log.info("(--) Closing previously open SHORT position at [shortOpenPrice]: " + shortOpenPrice);
                                log.info("(--) Closing previously open SHORT position at [ask]: " + ask.toDouble());
                                log.info("(--) Closing previously open SHORT position at [tradeProfit]: " + tradeProfit);
                                log.info("(--) Closing previously open SHORT position at [cumProfit]: " + cumProfit);
                            }
                        }

                        openShort = true;
                        shortOpenPrice = bid.toDouble();
                        //log.info("tradeProfit " + tradeProfit);
                        //log.info("cumProfit " + cumProfit);
                        */
                    if (!openShort) {
                        openShort = true;
                        shortOpenPrice = bid.toDouble();
                    }
                }

                //Long trade
                if (movingAverage <= ask.toDouble()) {
                    log.info("Long: at " + ask.toString());
                        /* Signal to BUY a long position
                        Here
                        1. There is already an existing open short position and this long position
                           will simply close that short by buying at ask and initiate a new position
                           at ask
                        2. The trade went sour and the signal is to establish a new long position at possibly
                           a lower price than before. Hence book loss by selling the existing long position
                           at bid and establishing a new long position at bid.
                         */
                    if (openShort) {
                        if ((shortOpenPrice - ask.toDouble()) > 0) {
                            openShort = false;
                            tradeProfit = shortOpenPrice - ask.toDouble(); //Buy to close short
                            cumProfit += tradeProfit;
                            log.info("(++) Closing previously open Short position at [shortOpenPrice]: " + shortOpenPrice);
                            log.info("(++) Closing previously open Short position at [ask]: " + ask.toDouble());
                            log.info("(++) Closing previously open Short position at [tradeProfit]: " + tradeProfit);
                            log.info("(++) Closing previously open Short position at [cumProfit]: " + cumProfit);
                        }

                    }
                        /*if (openLong) {
                            //longClose (loss)
                            if((ask.toDouble() - longOpenPrice ) > (bid.toDouble() - longOpenPrice)) {
                                tradeProfit = bid.toDouble() - longOpenPrice;
                                cumProfit += tradeProfit;
                                openLong = false;
                                log.info("(--) Closing previously open LONG position at [longOpenPrice]: " + longOpenPrice);
                                log.info("(--) Closing previously open LONG position at [bid]: " + bid.toDouble());
                                log.info("(--) Closing previously open LONG position at [tradeProfit]: " + tradeProfit);
                                log.info("(--) Closing previously open LONG position at [cumProfit]: " + cumProfit);
                            }

                        }

                        openLong = true;
                        longOpenPrice = ask.toDouble();
                        //log.info("tradeProfit " + tradeProfit);
                        //log.info("cumProfit " + cumProfit);
                        */
                    if (!openLong) {
                        openLong = true;
                        longOpenPrice = ask.toDouble();
                    }

                }

            }

            if (dataPoints >= _N_period) {

                Statistics pointStat = new Statistics(_priceData);

                movingAverage = pointStat.getMean();
                upperBand = movingAverage + _k_stdDev * pointStat.getStdDev();
                lowerBand = movingAverage - _k_stdDev * pointStat.getStdDev();

                //upperBand = (Decimal.fromDouble(upperBand)).roundToDigits(2).toDouble();
                //lowerBand = (Decimal.fromDouble(lowerBand)).roundToDigits(2).toDouble();

                    /*log.info("movingAverage: " + movingAverage);
                    log.info("_k_stdDev : " + _k_stdDev );
                    log.info("stdDev: " +   pointStat.getStdDev());
                    log.info("_k_stdDev * stdDev: " + _k_stdDev * pointStat.getStdDev());

                    log.info("upperBand: " + upperBand);
                    log.info("lowerBand: " + lowerBand);*/
            }
        }


    }

    private Decimal getNewBid(Decimal oldBid) {
        Decimal bidRelative = oldBid.multiply(Decimal.fromDouble(0.995));
        Decimal bidAbsolute = oldBid.subtract(Decimal.fromDouble(0.05));
        return Decimal.min(bidRelative, bidAbsolute);
    }

    private void sendOrderBidSide(Decimal bid) {
        lastBidOrder = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.BID)
                .withLimit(bid)
                .withSize(Decimal.fromDouble(0.01));
        receivedOrderReceiptBidSide = false;
        sendNewOrder(lastBidOrder);
    }

    private void sendOrderAskSide(Decimal ask) {
        lastBidOrder = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(BookSide.ASK)
                .withLimit(ask)
                .withSize(Decimal.fromDouble(0.01));
        receivedOrderReceiptAskSide = false;
        sendNewOrder(lastBidOrder);
    }

    private void cancelLastOrderBidSide() {
        OrderCancel o = lastBidOrder.createCancelOrder();
        sendOrderCancel(o);
        lastBidOrder = OrderNew.NULL;
        receivedOrderReceiptBidSide = false;
    }

    private boolean isInMarketBidSide() {
        return lastBidOrder != OrderNew.NULL;
    }


    public BollingerBands() {
        super();
        lastBidOrder = OrderNew.NULL;
        shuttingDown = false;
        previousPriceData = PriceData.NULL;
        position = Decimal.ZERO;
        _N_period = 20;
        _k_stdDev = 2;
        dataPoints = -1;
        _priceData = new double[_N_period];
        stat = new Statistics(_priceData);
        openShort = false;
        openLong = false;
        longOpenPrice = 0.0;
        shortOpenPrice = 0.0;
        cumProfit = 0.0;


    }

    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private OrderNew lastBidOrder;
    private PriceData previousPriceData;
    private boolean receivedOrderReceiptBidSide;
    private boolean receivedOrderReceiptAskSide;
    private int _N_period;
    private int _k_stdDev;
    private double[] _priceData;
    private double movingAverage;
    private double upperBand;
    private double lowerBand;
    private long dataPoints;
    private Statistics stat;
    private boolean openShort, openLong;
    private double shortOpenPrice, longOpenPrice;
    private double cumProfit;
    private double oldBidPrice, oldAskPrice, oldMidPrice;

}
