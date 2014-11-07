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
    final String positionMultiplyer = "10000";

    final int roundToDigits = 5;


    private class PriceDataBollingerBands extends Observable {

        PriceDataBollingerBands(String productId, int movingWindowSize){
            this.productId = productId;
            bollingerWindow = new BigInteger("" + movingWindowSize);
            listIndex = BigInteger.ZERO;
            triggeredTrade = NONE;
            openTrade = NONE;
            cumProfit = 0.0;
            priceDataList = new LinkedList<PriceData>();
            lastAskOrder = OrderNew.NULL;
            lastBidOrder = OrderNew.NULL;

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
                    //sendOrder(BookSide.BID, p.getBid().subtract(Decimal.fromString("0.001")), Decimal.fromString("1"));
                }
            }
            else
            {
                if(!priceDataList.peekFirst().isSameFrontRowPricesAs(p)){ // only Unique prices
                    //triggerTrades(p);

                    placeBuyOrderAtLowerBand();
                    placeSellOrderAtUpperBand();



                    if(isThereOpenLong() && !isThereMeanSellOrder())
                        placeSellOrderAtMean();


                    if(isThereOpenShort() && !isThereMeanBuyOrder())
                        placeBuyOrderAtMean();


                    priceDataList.removeFirst();
                    priceDataList.add(p);
                }
            }

        }

        private OrderNew sendOrder(BookSide side, Decimal limitPrice, Decimal orderSize)
        {
            OrderNew order = OrderNew.create()
                    .withProductId(productId)
                    .withInternalAccount(getInternalAccount())
                    .withBookSide(side)
                    .withLimit(limitPrice)
                    .withSize(orderSize.multiply(Decimal.fromString(positionMultiplyer)));
            sendNewOrder(order);
            return order;
        }

        private void sendBuyOrder(){
            Decimal lowerBollingerBand = Decimal.fromDouble(getLowerBollingerBand(BookSide.NULL));
            lowerBollingerBand = lowerBollingerBand.roundToDigits(roundToDigits);
            lastBidOrder = sendOrder(BookSide.BID,lowerBollingerBand,Decimal.ONE);
            this.bidOrderAtUpperBandLimitReceipt = false;
            log.info("(++) Sending Order {}"+ lastBidOrder.toString());
        }



        private void placeBuyOrderAtLowerBand(){

            if(!isThereOpenPosition()){
                if(lastBidOrder.equals(OrderNew.NULL)) sendBuyOrder();

                if(this.bidOrderAtUpperBandLimitReceipt){
                    OrderCancel o = lastBidOrder.createCancelOrder();
                    sendOrderCancel(o);
                    lastBidOrder = OrderNew.NULL;
                    sendBuyOrder();
                }
            }

        }

        private boolean isThereMeanSellOrder(){
            return (meanSellOrder == null)? false: true;
            // true when there
        }

        private boolean isThereMeanBuyOrder(){
            return (meanBuyOrder == null)? false: true;
            // true when there
        }

        private void placeSellOrderAtMean(){
            Decimal meanPrice = Decimal.fromDouble(getPriceMean(BookSide.NULL));
            meanPrice = meanPrice.roundToDigits(roundToDigits);

            if (meanSellOrder == null)
                sendMeanSellOrder(meanPrice);

            if (this.meanSellOrderReceipt) {
                double lastBidOrderLimit =  lastBidOrder.getLimit().toDouble();
                double midPrice = meanPrice.toDouble();
                if(meanPrice.isGreaterThan(lastBidOrder.getLimit())){
                    OrderCancel o = meanSellOrder.createCancelOrder();
                    sendOrderCancel(o);
                    meanSellOrder = null;
                    sendMeanSellOrder(meanPrice);
                }


            }
        }

        private void sendMeanSellOrder(Decimal price){
            meanSellOrder = sendOrder(BookSide.ASK,price,Decimal.ONE);
            this.meanSellOrderReceipt = false;
            log.info("(++) Sending Mean Order {}"+ lastBidOrder.toString());
        }

        private void sendMeanBuyOrder(Decimal price){
            meanBuyOrder = sendOrder(BookSide.BID,price,Decimal.ONE);
            this.meanBuyOrderReceipt = false;
            log.info("(++) Sending Mean Order {}"+ lastBidOrder.toString());
        }


        private void sendSellOrder(){
            Decimal upperBollingerBand = Decimal.fromDouble(getUpperBollingerBand(BookSide.NULL));
            upperBollingerBand = upperBollingerBand.roundToDigits(roundToDigits);
            lastAskOrder = sendOrder(BookSide.ASK,upperBollingerBand,Decimal.ONE);
            this.askOrderAtLowerBandLimitReceipt = false;
            log.info("(++) Sending Order {}"+ lastAskOrder.toString());

        }


        private void placeSellOrderAtUpperBand(){
            if(!isThereOpenPosition()){
                if(lastAskOrder.equals(OrderNew.NULL))sendSellOrder();

                if(this.askOrderAtLowerBandLimitReceipt){
                    OrderCancel o = lastAskOrder.createCancelOrder();
                    sendOrderCancel(o);
                    lastAskOrder = OrderNew.NULL;
                    sendSellOrder();
                }

            }
        }

        private void placeBuyOrderAtMean(){
            Decimal meanPrice = Decimal.fromDouble(getPriceMean(BookSide.NULL));
            meanPrice = meanPrice.roundToDigits(roundToDigits);

            if (meanBuyOrder == null) sendMeanBuyOrder(meanPrice);

            if (this.meanBuyOrderReceipt) {

                double lastAskOrderLimit =  lastAskOrder.getLimit().toDouble();
                double midPrice = meanPrice.toDouble();
                if(meanPrice.isLessThan(lastAskOrder.getLimit())){
                    OrderCancel o = meanBuyOrder.createCancelOrder();
                    sendOrderCancel(o);
                    meanBuyOrder = OrderNew.NULL;
                    sendMeanSellOrder(meanPrice);
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

        private boolean isThereOpenPosition(){
            return openTrade.equals(NONE) ? false : true;
        }

        private boolean isThereOpenShort(){
            return openTrade.equals(SHORT) ? true : false;
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

        public double getCumProfit(){
            return cumProfit;
        }

        public boolean getAskOrderAtLowerBandLimitReceipt(){
            return this.askOrderAtLowerBandLimitReceipt;
        }


        public boolean getBidOrderAtUpperBandLimitReceipt(){
            return this.bidOrderAtUpperBandLimitReceipt;
        }

        private String productId;
        private LinkedList<PriceData> priceDataList;
        private BigInteger listIndex;
        private BigInteger bollingerWindow;
        private double upperBollingerBand, lowerBollingerBand;
        private  String triggeredTrade , openTrade;
        private OrderNew lastBidOrder, lastAskOrder, meanBuyOrder, meanSellOrder;
        double openLongAsk , openShortBid , cumProfit;
        public boolean askOrderAtLowerBandLimitReceipt,bidOrderAtUpperBandLimitReceipt;
        public boolean meanBuyOrderReceipt, meanSellOrderReceipt;
    }


    @Override
    public void update(Observable priceDataBollingerBands, Object arg) {
        String productId = (String) arg;
        log.info("Notifying " + productId + " with profit = " + ((PriceDataBollingerBands)priceDataBollingerBands).getCumProfit());
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

        //log.info("Received price #" + counter + ":" + bollingerBandMap.get(priceData.getProductId()).toString());
        bollingerBandMap.get(priceData.getProductId()).addPrice(priceData);
        counter++;

    }

    @Override
    public void onReceiptForStrategy(Receipt receipt) {
        if (shuttingDown) return;
        if (receipt.getRejectReason().length() > 0) {
            log.error("Received rejection! Stopping for now!");
            //System.exit(-1);
        }

        boolean foundProductForReceipt = false;
        String productId = "";

        for(String tradeProductId : pidList) {
            if(receipt.hasProductId(tradeProductId)){
                foundProductForReceipt = true;
                productId = tradeProductId;
            }
        }

        if (!foundProductForReceipt) {
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }

        if(foundProductForReceipt){
            PriceDataBollingerBands pbands = bollingerBandMap.get(productId);

            if (receipt.isForOrder(pbands.lastAskOrder)) {
                pbands.askOrderAtLowerBandLimitReceipt = true;
                if(!pbands.isThereOpenLong() && receipt.isEndState())
                {
                    pbands.openTrade = SHORT;
                }

            }

            if (receipt.isForOrder(pbands.lastBidOrder)) {
                pbands.bidOrderAtUpperBandLimitReceipt = true;
                if(!pbands.isThereOpenShort() && receipt.isEndState()){
                    pbands.openTrade = LONG;
                }
            }

            if (pbands.meanBuyOrder !=null && receipt.isForOrder(pbands.meanBuyOrder)) {
                pbands.meanBuyOrderReceipt = true;
                if(pbands.isThereOpenShort() && receipt.isEndState() ){
                    pbands.openTrade = NONE;
                    pbands.lastAskOrder = OrderNew.NULL;
                    pbands.meanBuyOrder = null;
                }
            }

            if (pbands.meanSellOrder != null && receipt.isForOrder(pbands.meanSellOrder)) {
                pbands.meanSellOrderReceipt = true;
                if(pbands.isThereOpenLong() && receipt.isEndState() ){
                    pbands.openTrade = NONE;
                    pbands.lastBidOrder = OrderNew.NULL;
                    pbands.meanSellOrder = null;
                }
            }

            log.debug("Received receipt: " + receipt);
            //position = receipt.getPositionChangeOfBase().add(position);
        }

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
        startStrategy();
    }

    @Override
    public void onShutdown() {
        shuttingDown = true;
        //if (isInMarketBidSide() && receivedOrderReceiptBidSide) cancelLastOrderBidSide();
    }

    public BollingerBands() {
        super();
        shuttingDown = false;

    }

    private boolean shuttingDown;
    private String tradeProductList ;
    private int counter;
    private ConcurrentHashMap<String,PriceDataBollingerBands> bollingerBandMap;
    private  List<String> pidList;
    private boolean hasPriceData;



}
