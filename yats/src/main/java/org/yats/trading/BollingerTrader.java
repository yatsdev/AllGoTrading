package org.yats.trading;

import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by abbanerjee on 05/11/14.
 */
public class BollingerTrader {

    final static String productOrderSize = "1000";
    final int precisionForPrice = 5;

    public BollingerTrader(BollingerBandsForProduct bandsForProduct, BookSide bookSide){
        this.bandsForProduct = bandsForProduct;
        takePositionOrder = OrderNew.NULL;
        closePositionOrder = OrderNew.NULL;
        lastSentOrder = OrderNew.NULL;
        cancelledOrder = OrderCancel.create();
        openPositionReceiptRecieved = false;
        closePositionReceiptRecieved = false;
        lastSentOrderReceiptRecieved = false;
        openLimitFilled= false;
        closeLimitFilled = false;
        this._bookSide = bookSide;
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
        if(r.isForOrder(lastSentOrder)){
            lastSentOrderReceiptRecieved = true;
            if(r.isEndState()){
                if(!openLimitFilled){
                    openLimitFilled = true;
                    closeLimitFilled = !openLimitFilled;
                    takePositionOrder = lastSentOrder;
                }
                if(openLimitFilled && !closeLimitFilled){
                    closePositionOrder = lastSentOrder;
                    closeLimitFilled = true;
                    openLimitFilled = !closeLimitFilled;
                }
            }
        }
    }


    private BollingerBandsForProduct bandsForProduct;
    public OrderNew takePositionOrder, closePositionOrder, lastSentOrder;
    public OrderCancel cancelledOrder;
    public boolean openPositionReceiptRecieved;
    public boolean closePositionReceiptRecieved, lastSentOrderReceiptRecieved;
    public boolean openLimitFilled, closeLimitFilled;
    public Decimal takeProfitPrice;
    private BookSide _bookSide;

}
