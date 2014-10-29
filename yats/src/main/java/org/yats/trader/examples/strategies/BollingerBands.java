package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.Statistics;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.awt.print.Book;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by macbook52 on 11/10/14.
 * Bollinger Bands consist of:
 * <p/>
 * an N-period moving average (MA)
 * an upper band at K times an N-period standard deviation above the moving average (MA + Kσ)
 * a lower band at K times an N-period standard deviation below the moving average (MA − Kσ)
 *
 */

public class BollingerBands extends StrategyBase  implements Observer  {
    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(BollingerBands.class);
    final int BOLLINGER_WINDOW_SIZE = 20;
    final String LONG = "LONG";
    final String SHORT = "SHORT";
    final String NONE = "NONE";


    private class PriceDataBollingerBands extends Observable {

        PriceDataBollingerBands(String productId, int movingWindowSize){
            this.productId = productId;
            bollingerWindow = new BigInteger("" + movingWindowSize);
            listIndex = BigInteger.ZERO;
            triggeredTrade = NONE;
            openTrade = NONE;
            priceDataList = new LinkedList<PriceData>();

        }

        public void setTradeListener(Observer observer){
            if(observer != null){
                this.addObserver(observer);
            }
        }

        public void addPrice(PriceData p)
        {
            int listCount = priceDataList.size();
            listIndex = listIndex.add(BigInteger.ONE);
            if(listCount < bollingerWindow.intValue())
            {
                if(priceDataList.size() == 0){
                    priceDataList.add(p);
                }
                if(!priceDataList.peekFirst().isSameFrontRowPricesAs(p)){ // only Unique prices
                    priceDataList.add(p);
                }
            }
            else
            {
                if(!priceDataList.peekFirst().isSameFrontRowPricesAs(p)){ // only Unique prices
                    triggerTrades(p);
                    priceDataList.removeFirst();
                    priceDataList.add(p);
                }
            }
        }

        private boolean isBidAboveUpperBand(PriceData p, double upperBand){
            return p.getBid().toDouble() > upperBand ? true: false;
        }

        private boolean isAskBelowLowerBand(PriceData p, double lowerBand){
            return p.getAsk().toDouble() < lowerBand ? true: false;
        }

        private boolean isMidBelowMean(PriceData p, double meanMid){
            double priceMid = (p.getAsk().toDouble() + p.getBid().toDouble()) / 2.0;
            return priceMid < meanMid ? true: false;
        }

        private boolean isMidAboveMean(PriceData p, double meanMid){
             return !isMidBelowMean(p, meanMid);
        }

        private boolean isThereOpenLong(){
            return openTrade.equals(LONG) ? true : false;
        }

        private boolean isThereOpenShort(){
            return openTrade.equals(SHORT) ? true : false;
        }

        private void closeAlreadyOpenShort(){
            this.openTrade = NONE;
            this.triggeredTrade = LONG;
        }


        private boolean openLong(PriceData p){
            String lastTradeTriggered = triggeredTrade;

            if(isThereOpenShort() && isMidBelowMean(p,getPriceMean(BookSide.NULL))){
                //
                double tradeProfit = openShortBid - p.getAsk().toDouble();
                if(tradeProfit > 0){
                    log.info("(++) Profit: " + tradeProfit);
                    closeAlreadyOpenShort();
                }

            }
            if(!isThereOpenShort() && isAskBelowLowerBand(p,getLowerBollingerBand(BookSide.NULL))){
                openLongAsk = p.getAsk().toDouble();
                this.triggeredTrade = LONG;
            }

            if(lastTradeTriggered.equals(triggeredTrade)){
                setChanged();
                notifyObservers(productId);
            }

            return false;
        }
        private void closeAlreadyOpenLong(){
            this.openTrade = NONE;
            this.triggeredTrade = SHORT;
        }

        private boolean openShort(PriceData p){
            String lastTradeTriggered = triggeredTrade;
            if(isThereOpenLong() && isMidAboveMean(p,getPriceMean(BookSide.NULL))){
                double tradeProfit = openLongAsk - p.getBid().toDouble();
                if(tradeProfit > 0){
                    log.info("(++) Profit: " + tradeProfit);
                    closeAlreadyOpenLong();
                }
            }

            if(!isThereOpenLong() && isBidAboveUpperBand(p,getLowerBollingerBand(BookSide.NULL))){
                openShortBid = p.getBid().toDouble();
                this.triggeredTrade = SHORT;
            }

            if(lastTradeTriggered.equals(triggeredTrade)){
                setChanged();
                notifyObservers(productId);
            }
            return false;
        }

        private void triggerTrades(PriceData p){
            double bidPrice = p.getBid().toDouble();
            double askPrice = p.getAsk().toDouble();
            double upperBand = getUpperBollingerBand(BookSide.NULL);
            double mean = getPriceMean(BookSide.NULL);
            double lowerBand = getLowerBollingerBand(BookSide.NULL);

            openLong(p);
            openShort(p);



        }

        private double[] getPriceBids(){
            if(listIndex.intValue() > bollingerWindow.intValue())
            {
                double[] listBids = new double[bollingerWindow.intValue()];
                int iteratorIndex = 0;
                Iterator<PriceData> priceDataIterator = priceDataList.iterator();
                while(priceDataIterator.hasNext())
                {
                    listBids[iteratorIndex++]= priceDataIterator.next().getBid().toDouble();
                }
                return listBids;
            }
            return null;
        }

        private double[] getPriceAsks() {
            if (listIndex.intValue() > bollingerWindow.intValue()) {
                double[] listAsks = new double[bollingerWindow.intValue()];
                int iteratorIndex = 0;
                Iterator<PriceData> priceDataIterator = priceDataList.iterator();
                while (priceDataIterator.hasNext()) {
                    listAsks[iteratorIndex++] = priceDataIterator.next().getAsk().toDouble();
                }
                return listAsks;

            }
            return null;
        }

        private double[] getMidPrices(){
            if (listIndex.intValue() > bollingerWindow.intValue()) {
                double[] listMidPrices = new double[bollingerWindow.intValue()];
                int iteratorIndex = 0;
                Iterator<PriceData> priceDataIterator = priceDataList.iterator();
                while (priceDataIterator.hasNext()) {
                    PriceData priceData = priceDataIterator.next();
                    listMidPrices[iteratorIndex++] = (priceData.getAsk().toDouble() + priceData.getBid().toDouble()) / 2.0;
                }
                return listMidPrices;

            }
            return null;
        }

        private double getPriceMean(BookSide bookSide){
            Statistics priceStats;
            if(bookSide.equals(BookSide.NULL)){
                priceStats = new Statistics(getMidPrices());
            }
            else if(bookSide.equals(BookSide.ASK)){
                priceStats = new Statistics(getPriceAsks());
            }
            else{
                priceStats = new Statistics(getPriceBids());
            }
            return priceStats.getMean();
        }

        private double getStandardDev(BookSide bookSide){
            Statistics priceStats = null;
            if(bookSide.equals(BookSide.NULL)){
                priceStats = new Statistics(getMidPrices());
            }
            else if(bookSide.equals(BookSide.ASK)){
                priceStats = new Statistics(getPriceAsks());
            }
            else if (bookSide.equals(BookSide.BID)){
                priceStats = new Statistics(getPriceBids());
            }
            return priceStats.getStdDev();
        }

        private double getUpperBollingerBand(BookSide bookSide){
            upperBollingerBand = (getPriceMean(bookSide) + 2* getStandardDev(bookSide));
            return upperBollingerBand;
        }

        private double getLowerBollingerBand(BookSide bookSide){
            lowerBollingerBand = (getPriceMean(bookSide) - 2* getStandardDev(bookSide));
            return lowerBollingerBand;
        }

        public  String toString(){
            String returnString = "[" +  listIndex.intValue() + "]{" + this.productId + "} " + "Not Initiailized";
            if(listIndex.intValue() > bollingerWindow.intValue()){
                returnString  = "{ {" + this.productId + "}[" + listIndex.intValue() + "] prices with Mean [" + getPriceMean(BookSide.NULL)  + "] and Bands [" + getLowerBollingerBand(BookSide.NULL) + "," + getUpperBollingerBand(BookSide.NULL) + "] }";
            }

            return returnString;
        }

        public String getTriggeredTrade(){
            return triggeredTrade;
        }

        public String getOpenTrade(){
            return openTrade;
        }

        private String productId;
        private LinkedList<PriceData> priceDataList;
        private BigInteger listIndex;
        private BigInteger bollingerWindow;
        private double upperBollingerBand, lowerBollingerBand;
        private  String triggeredTrade , openTrade;
        double openLongAsk , openShortBid;
    }


    @Override
    public void update(Observable priceDataBollingerBands, Object arg) {
        String productId = (String) arg;
        log.info("Notifying " + productId + " with open as " + ((PriceDataBollingerBands)priceDataBollingerBands).getOpenTrade() + " and closing trade " + ((PriceDataBollingerBands)priceDataBollingerBands).getTriggeredTrade());
    }

    @Override
    public void onPriceDataForStrategy(PriceData priceData) {
        if (shuttingDown) return;
        if (!isInitialised()) return;

        hasPriceData = false;
        for(String tradeProductId : pidList) {
            if (priceData.hasProductId(tradeProductId)) {
                hasPriceData = true;
            }
        }
        if(!hasPriceData) return;

        log.info("Received price #" + counter + ":" + bollingerBandMap.get(priceData.getProductId()).toString());
        bollingerBandMap.get(priceData.getProductId()).addPrice(priceData);
        counter++;

        //handlePriceDataBidSide(priceData);
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
        tradeProductList = getConfig("productIdList");
        String[] parts = tradeProductList.split(",");
        pidList = Arrays.asList(parts);
        bollingerBandMap = new ConcurrentHashMap<String, PriceDataBollingerBands>();
        for(String tradeProductId : pidList) {
            PriceDataBollingerBands bollingerBandForProduct = new PriceDataBollingerBands(tradeProductId,BOLLINGER_WINDOW_SIZE);
            bollingerBandForProduct.setTradeListener(this);
            bollingerBandMap.put(tradeProductId,bollingerBandForProduct);
            subscribe(tradeProductId);
        }
    }

    @Override
    public void onShutdown() {
        shuttingDown = true;
        if (isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
    }

    private void handlePriceDataBidSide(PriceData priceData) {




        /*
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

                if(midPrice >= upperBand){
                    closeShortPosition(ask.toDouble());
                    openShortPosition(bid.toDouble());
                }

                if(midPrice <= lowerBand){
                    closeLongPosition(bid.toDouble());
                    openLongPosition(ask.toDouble());
                }

                if (movingAverage >= bid.toDouble()) {
                    closeLongPosition(bid.toDouble());
                }

                //Long trade
                    if (movingAverage <= ask.toDouble()) {

                    closeShortPosition(ask.toDouble());

                }

            }

            if (dataPoints >= _N_period) {

                Statistics pointStat = new Statistics(_priceData);

                movingAverage = pointStat.getMean();
                upperBand = movingAverage + _k_stdDev * pointStat.getStdDev();
                lowerBand = movingAverage - _k_stdDev * pointStat.getStdDev();

                //upperBand = (Decimal.fromDouble(upperBand)).roundToDigits(2).toDouble();
                //lowerBand = (Decimal.fromDouble(lowerBand)).roundToDigits(2).toDouble();

                    log.info("movingAverage: " + movingAverage);
                    //log.info("_k_stdDev : " + _k_stdDev );
                    //log.info("stdDev: " +   pointStat.getStdDev());
                    //log.info("_k_stdDev * stdDev: " + _k_stdDev * pointStat.getStdDev());

                    log.info("upperBand: " + upperBand);
                    log.info("lowerBand: " + lowerBand);
            }
        }
*/

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

    private void openLongPosition(double askPrice){
        if(openShort){
            closeShortPosition(askPrice);
        }
        else{
            if (!openLong) {
                openLong = true;
                longOpenPrice = askPrice;
                log.info("(++) Open Long position at  [longOpenPrice]: " + longOpenPrice);
            }

        }


    }

    private void closeLongPosition(double bidPrice){
    /* Signal to close any open long position
            Here
            1. There is already an existing open long position and this short position
               will simply close that long by selling at bid and initiate a new position
               at bid
    */


        if (openLong) {
            log.info("(--) Current Trade [bidPrice - longOpenPrice]: " + bidPrice + "-" +  longOpenPrice);
            if ((bidPrice - longOpenPrice) > 0) {
                openLong = false;
                double tradeProfit = bidPrice - longOpenPrice;
                cumProfit += tradeProfit;
                log.info("(++) Closing previously open Long position at [longOpenPrice]: " + longOpenPrice);
                log.info("(++) Closing previously open Long position at [bid]: " + bidPrice);
                log.info("(++) Closing previously open Long position at [tradeProfit]: " + tradeProfit);
                log.info("(++) Closing previously open Long position at [cumProfit]: " + cumProfit);
            }

        }



    }

    private void openShortPosition(double bidPrice){
        if(openLong){
            closeLongPosition(bidPrice);
        }
        else{
            if (!openShort) {
                openShort = true;
                shortOpenPrice = bidPrice;

                log.info("(++) Open Short position at  [shortOpenPrice]: " + shortOpenPrice);
            }
        }


    }

    private void closeShortPosition(double askPrice){
    /* Signal close any existing short position for profit
    Here
    1. There is already an existing open short position and this long position
       will simply close that short by buying at ask and initiate a new position
       at ask

     */
        if (openShort) {
            log.info("(--) Current Trade [shortOpenPrice - askPrice]: " + shortOpenPrice + "-" +  askPrice);
            if ((shortOpenPrice - askPrice) > 0) {
                openShort = false;
                double tradeProfit = shortOpenPrice - askPrice; //Buy to close short
                cumProfit += tradeProfit;
                log.info("(++) Closing previously open Short position at [shortOpenPrice]: " + shortOpenPrice);
                log.info("(++) Closing previously open Short position at [ask]: " + askPrice);
                log.info("(++) Closing previously open Short position at [tradeProfit]: " + tradeProfit);
                log.info("(++) Closing previously open Short position at [cumProfit]: " + cumProfit);
            }

        }
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

    private String tradeProductList;
    private int counter;
    private ConcurrentHashMap<String,PriceDataBollingerBands> bollingerBandMap;
    private  List<String> pidList;
    private boolean hasPriceData;



}
