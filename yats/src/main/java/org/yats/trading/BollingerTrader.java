package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.trader.StrategyRunner;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by abbanerjee on 05/11/14.
 */
public class BollingerTrader {

    final static String productOrderSize = "1000";
    final int precisionForPrice = 5;
    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);

    public BollingerTrader(BollingerBandsForProduct bandsForProduct, BookSide bookSide){
        this.bandsForProduct = bandsForProduct;
        resetVariables();
        this._bookSide = bookSide;
    }

    private void resetVariables(){
        takePositionOrder = OrderNew.NULL;
        closePositionOrder = OrderNew.NULL;
        lastSentOrder = OrderNew.NULL;
        cancelledOrder = OrderCancel.create();
        openPositionReceiptRecieved = false;
        closePositionReceiptRecieved = false;
        lastSentOrderReceiptRecieved = false;
        openLimitFilled= false;
        closeLimitFilled = false;
        firstTimeLegOneOrder = false;
        firstTimeLegTwoOrder = false;


    }

    private OrderNew generateNewOrder(BookSide side, Decimal limitPrice)
    {
        OrderNew newOrder = OrderNew.create()
                .withProductId(bandsForProduct.getProductId())
                .withInternalAccount(bandsForProduct.getInternalAccount())
                .withBookSide(side)
                .withLimit(limitPrice)
                .withSize(Decimal.fromString(productOrderSize));
        return newOrder;
    }

    public OrderNew getNextOpeningOrder() {
        OrderNew newOrder = OrderNew.NULL;

        Decimal limitPrice = Decimal.CENT;
        if (_bookSide.equals(BookSide.BID)) {
            limitPrice = Decimal.fromDouble(bandsForProduct.getLowerBoundForProduct());
        } else {
            limitPrice = Decimal.fromDouble(bandsForProduct.getUpperBoundForProduct());
        }
        newOrder = generateNewOrder(_bookSide, limitPrice.roundToDigits(precisionForPrice));
        return newOrder;
    }

    public OrderNew getCloseOrder(){
        Decimal limitPrice = Decimal.fromDouble(bandsForProduct.getAverageBidAndAsk());
        OrderNew orderNew = generateNewOrder(_bookSide.toOpposite(), limitPrice.roundToDigits(precisionForPrice));
        return orderNew;
    }

    public void onReceipt(Receipt r){

        if(r.isEndState() && r.getCurrentTradedSize().toInt() == 0){
            log.info("Receipt Cancelled " + r.toString());
        }

        if(r.isEndState() && r.getTotalTradedSize().toInt() > 0){
            log.info("Receipt filled " + r.toString());
            if(r.isForOrder(lastSentOrder)){
                lastSentOrderReceiptRecieved = true;
                openLimitFilled = true;
                takePositionOrder = lastSentOrder;
            }
            if(r.isForOrder(closePositionOrder)){
                resetVariables();
            }
        }

        if(r.isForOrder(lastSentOrder)) {
            lastSentOrderReceiptRecieved = true;
        }
        if(r.isForOrder(closePositionOrder)){
            closePositionReceiptRecieved = true;
        }
    }


    private BollingerBandsForProduct bandsForProduct;
    public OrderNew takePositionOrder, closePositionOrder, lastSentOrder;
    public OrderCancel cancelledOrder;
    public boolean openPositionReceiptRecieved;
    public boolean closePositionReceiptRecieved, lastSentOrderReceiptRecieved;
    public boolean openLimitFilled, closeLimitFilled, firstTimeLegOneOrder, firstTimeLegTwoOrder;
    public Decimal takeProfitPrice;
    private BookSide _bookSide;

}
