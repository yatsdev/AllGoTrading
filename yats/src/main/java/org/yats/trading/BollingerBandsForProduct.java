package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.Statistics;
import org.yats.trader.examples.strategies.BollingerBands;
import org.yats.trader.examples.strategies.GameBollingerBands;

import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Created by abbanerjee on 05/11/14.
 */

 public class BollingerBandsForProduct{

    final static int precisionForPrice = 5;
    final Logger log = LoggerFactory.getLogger(BollingerBandsForProduct.class);



    public BollingerBandsForProduct(String productId, int movingWindowSize){
        this.productId = productId;
        listIndex = BigInteger.ZERO;
        priceDataList = new LinkedList<PriceData>();
        this.movingWindowSize = BigInteger.valueOf((long) movingWindowSize);
        longTrader= new BollingerTrader(this,BookSide.BID);
        shortTrader =new BollingerTrader(this,BookSide.ASK);
        firstBuySent = false;
        firstSellSent = false;
        closeBuySent = false;
        closeSellSent = false;
    }

    public boolean isEligibleToSendOrders(){
        return (movingWindowSize.intValue() == priceDataList.size());
    }

    public String getProductId(){
        return this.productId;
    }

    public void addPrice(PriceData p){

        listIndex = listIndex.add(BigInteger.ONE);
        if(listIndex.intValue() <= movingWindowSize.intValue()){
            if(priceDataList.size() == 0){
                priceDataList.add(p);
            }
            if(!priceDataList.peekFirst().isSameFrontRowPricesAs(p)){ // only Unique prices
                priceDataList.add(p);
            }
        }
        else{
            if(!priceDataList.peekFirst().isSameFrontRowPricesAs(p)){ // only Unique prices
                //log.info("PriceList.size() = " + priceDataList.size());
                //generateBuyOrder(p);
                generateSellOrder(p);

                priceDataList.add(p);
                priceDataList.removeFirst();

                assert(priceDataList.size() == 20);
            }
        }

    }

    private void generateSellOrder(PriceData marketPrice){
        if(!shortTrader.openLimitFilled){
            OrderNew newLimitSellOrder  = shortTrader.getNextOpeningOrder();
            sendOrRenewLimitSellOrder(newLimitSellOrder);
        }
        else if(shortTrader.openLimitFilled ){
            OrderNew newCloseOrder = shortTrader.getCloseOrder();
            sendOrRenewBuyClosingOrder(newCloseOrder);
        }
    }


    private void generateBuyOrder(PriceData marketPrice){
        if(!longTrader.openLimitFilled)
        {
            OrderNew newLimitBuyOrder  = longTrader.getNextOpeningOrder();
            sendOrRenewLimitBuyOrder(newLimitBuyOrder);
        }
        else if(longTrader.openLimitFilled ){
            OrderNew newCloseOrder = longTrader.getCloseOrder();
            sendOrRenewSellClosingOrder(newCloseOrder);
         }

    } //Signed off AAE 23:45 05.11.2014

    private void sendOrRenewLimitSellOrder(OrderNew newOrder){
        if(!shortTrader.openLimitFilled){
            if(!firstSellSent) {
                shortTrader.lastSentOrder = newOrder;
                orderSender.sendNewOrder(shortTrader.lastSentOrder);
                firstSellSent = true;
            }
            if(shortTrader.lastSentOrderReceiptRecieved){
                shortTrader.cancelledOrder = shortTrader.lastSentOrder.createCancelOrder();
                orderSender.sendOrderCancel(shortTrader.cancelledOrder);
                shortTrader.lastSentOrderReceiptRecieved = false;
                shortTrader.lastSentOrder =newOrder;
                orderSender.sendNewOrder(shortTrader.lastSentOrder);
            }
        }
    } // working AAE 23:18 05.11.2014

    private void sendOrRenewLimitBuyOrder(OrderNew newOrder){
        if(!longTrader.openLimitFilled){
            if(!firstBuySent) {
                longTrader.lastSentOrder = newOrder;
                orderSender.sendNewOrder(longTrader.lastSentOrder);
                firstBuySent = true;
            }
            if(longTrader.lastSentOrderReceiptRecieved){
                longTrader.cancelledOrder = longTrader.lastSentOrder.createCancelOrder();
                orderSender.sendOrderCancel(longTrader.cancelledOrder);
                longTrader.lastSentOrderReceiptRecieved = false;
                longTrader.lastSentOrder =newOrder;
                orderSender.sendNewOrder(longTrader.lastSentOrder);
            }
        }
    } // working AAE 23:18 05.11.2014

    private void sendOrRenewBuyClosingOrder(OrderNew newCloseOrder){

        if(!longTrader.closeLimitFilled){
            if(!closeBuySent) {
                longTrader.lastSentOrder =newCloseOrder;
                orderSender.sendNewOrder(newCloseOrder);
                closeBuySent = true;
            }
            if(longTrader.lastSentOrderReceiptRecieved){
                longTrader.lastSentOrderReceiptRecieved = false;
                OrderCancel cancelOrder = longTrader.lastSentOrder.createCancelOrder();
                orderSender.sendOrderCancel(cancelOrder);
                orderSender.sendNewOrder(newCloseOrder);

            }
        }

    }

    private void sendOrRenewSellClosingOrder(OrderNew newCloseOrder){
        if(!shortTrader.closeLimitFilled){
            if(!closeSellSent) {
                shortTrader.closePositionOrder =newCloseOrder;
                orderSender.sendNewOrder(newCloseOrder);
                closeSellSent = true;
            }
            if(shortTrader.closePositionReceiptRecieved){
                OrderCancel cancelOrder = shortTrader.closePositionOrder.createCancelOrder();
                orderSender.sendOrderCancel(cancelOrder);
                shortTrader.closePositionReceiptRecieved = false;
                shortTrader.closePositionOrder =newCloseOrder;
                orderSender.sendNewOrder(newCloseOrder);

            }
        }
    }


    public void onReceipt(Receipt r){
            //longTrader.onReceipt(r);
            shortTrader.onReceipt(r);

        }

        public double getAverageBidAndAsk(){
            double returnValue = 0;
            if (listIndex.intValue() >= movingWindowSize.intValue()) {
                for(int i = 0; i< priceDataList.size();i++){
                    double ask = priceDataList.get(i).getAsk().toDouble();
                    double bid = priceDataList.get(i).getBid().toDouble();
                    double mean = Decimal.fromDouble((bid + ask) /2.0).roundToDigits(precisionForPrice).toDouble();

                    returnValue = returnValue + mean;
                }
                double size = getMovingWindowSize();

                returnValue = returnValue / ((double) getMovingWindowSize() * 1.0);

            }
            return returnValue;
        }

        private double getStandardDeviationOfAverage(){
            double returnValue = 0 ;
            double meanArray[] = new double[priceDataList.size()];
            if (listIndex.intValue() >= movingWindowSize.intValue()) {
                for(int i = 0; i< priceDataList.size();i++){
                    double ask = priceDataList.get(i).getAsk().toDouble();
                    double bid = priceDataList.get(i).getBid().toDouble();
                    double mean = Decimal.fromDouble((bid + ask) /2.0).roundToDigits(precisionForPrice+1).toDouble();
                    meanArray[i] = mean;
                }
                Statistics priceStat = new Statistics(meanArray);
                returnValue = priceStat.getStdDev();

            }
            return returnValue;

        }

        public double getLowerBoundForProduct(){
            double returnValue = -1.0;

            returnValue = getAverageBidAndAsk() - 2.0 * getStandardDeviationOfAverage();

            return returnValue;
        }

        public double getUpperBoundForProduct(){
            double returnValue = -1.0;

            returnValue = getAverageBidAndAsk() + 2.0 * getStandardDeviationOfAverage();

            return returnValue;
        }

        public double Test_getStandardDeviationOfAverage(){
            return getStandardDeviationOfAverage();
        }

        public double Test_getAverageBidAndAsk(){
            return getAverageBidAndAsk();
        }

        public int getMovingWindowSize(){
            return this.movingWindowSize.intValue();
        }

        public int getPriceDataListSize(){
            return priceDataList.size();
        }

        public int getListIndexCount(){
            return listIndex.intValue();
        }

        public String getInternalAccount(){
            this.internalAccount = this.getClass().getSimpleName();
            return this.internalAccount;
        }

        public void setOrderSenderSuper(GameBollingerBands senderClass){
            this.orderSender = senderClass;
        }


        private String productId, internalAccount;
        private LinkedList<PriceData> priceDataList;
        private BigInteger listIndex, movingWindowSize;
        private BollingerTrader longTrader, shortTrader;
        private GameBollingerBands orderSender ;
        private boolean firstBuySent, firstSellSent, closeBuySent, closeSellSent;


    }
