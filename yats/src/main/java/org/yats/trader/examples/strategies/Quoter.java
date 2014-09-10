package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.HashMap;

public class Quoter extends StrategyBase {


    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(Quoter.class);

    @Override
    public void onPriceData(PriceData priceData)
    {
        if(!isInitialised()) return;
        if(shuttingDown) return;
        if(priceData.hasProductId(tradeProductId)) onTPPriceData(priceData);
        if(priceData.hasProductId(refProductId)) onRPPriceData(priceData);
    }

    public void onTPPriceData(PriceData priceData) {
        log.info("TradeProduct ticker: "+ priceData);
        log.info("TradeProduct depth: "+ priceData.getOfferBookAsCSV());
    }

    public void onRPPriceData(PriceData priceData) {
        log.info("RefProduct: "+ priceData);

        if(!priceData.isSameFrontRowBidAs(prevRefPriceData)) {
            cancelOrders(BookSide.BID);
            sendBidRelativeTo(priceData.getBid());
        }
        if(!priceData.isSameFrontRowAskAs(prevRefPriceData)) {
            cancelOrders(BookSide.ASK);
            sendAskRelativeTo(priceData.getAsk());
            sendReports(getReports());
        }

        prevRefPriceData = priceData;
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
        if(!isInitialised()) return;
        if(shuttingDown) return;

        log.info("received receipt: " + receipt);

        if(receipt.isEndState()) {
            orders.remove(receipt.getOrderId().toString());
            cancelMap.remove(receipt.getOrderIdString());
        }

        if(!receipt.hasProductId(tradeProductId)){
            log.error("received receipt for unknown product: " + receipt);
            return;
        }

        if(receipt.getRejectReason().length()>0) {
            log.error("received rejection "+receipt);
            orders.remove(receipt.getOrderId().toString());
            return;
//            close();
//            System.exit(-1);
        }

        position = position.add(receipt.getPositionChangeOfBase());
        log.info("position="+position);





    }

    @Override
    public void onSettings(IProvideProperties p) {
        System.out.println("Strategy settings: "+PropertiesReader.toString(p));
    }

    @Override
    public void init()
    {
        super.init();
        setInternalAccount(getConfig("internalAccount"));
        tradeProductId = getConfig("tradeProductId");
        refProductId = getConfig("referenceProductId");
        subscribe(tradeProductId);
        subscribe(refProductId);
        position = getPositionForProduct(tradeProductId);
        log.info("position="+position);
        tickSize =getConfigAsDecimal("tickSize");
        stepFactor=getConfigAsDouble("stepFactor");
        orderSize=getConfigAsDouble("orderSize");
        bidSteps=getConfigAsInt("bidSteps");
        askSteps=getConfigAsInt("askSteps");
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        cancelAllOrders();
    }

    private void sendBidRelativeTo(Decimal price) {
        double bidMarket=price.toDouble();
        for(int i=1; i<=bidSteps; i++) {
            double step = i;
            Decimal bidPrice = Decimal.fromDouble(bidMarket*(1.0-step*stepFactor)- tickSize.toDouble()).roundToTickSize(tickSize);
            sendOrderIfNotExisting(BookSide.BID, bidPrice);
            getReports().set("bidPrice"+i, bidPrice);
        }
    }

    private void sendAskRelativeTo(Decimal price) {
        double askMarket = price.toDouble();
        for (int i = 1; i <= askSteps; i++) {
            double step = i;
            Decimal askPrice = Decimal.fromDouble(askMarket * (1.0 + step * stepFactor) + tickSize.toDouble()).roundToTickSize(tickSize);
            sendOrderIfNotExisting(BookSide.ASK, askPrice);
            getReports().set("askPrice" + i, askPrice);
        }
    }

    private void sendOrderIfNotExisting(BookSide _side, Decimal _price) {
        if(!orderExists(_side, _price)) {
            sendOrder(_side, _price);
        }
    }

    private boolean orderExists(BookSide side, Decimal price) {
        for(OrderNew order : orders.values()) {
            boolean sameSide = order.isForBookSide(side);
            boolean samePrice = order.getLimit().isEqualTo(price);
            if(samePrice && sameSide) {
                return true;
            }
        }
        return false;
    }

    private void sendOrder(BookSide side, Decimal bid)
    {
        OrderNew order = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(side)
                .withLimit(bid)
                .withSize(Decimal.fromDouble(orderSize));
//        receivedOrderReceiptBidSide = false;
        orders.put(order.getOrderId().toString(), order);
        sendNewOrder(order);
    }

    private void cancelAllOrders() {
        for(OrderNew order : orders.values()) {
            OrderCancel o = order.createCancelOrder();
            sendOrderCancel(o);
        }
    }

    private void cancelOrders(BookSide side) {
        for(OrderNew order : orders.values()) {
            if(cancelMap.containsKey(order.getOrderIdString())) continue;
            if(order.isForBookSide(side)) {
                OrderCancel o = order.createCancelOrder();
                cancelMap.put(o.getOrderIdString(), o);
                sendOrderCancel(o);
            }
        }
    }


    public Quoter() {
        super();
        shuttingDown=false;
        position = Decimal.ZERO;
        orders = new HashMap<String, OrderNew>();
        cancelMap = new HashMap<String, OrderCancel>();
        prevRefPriceData = PriceData.NULL;

    }

    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private String refProductId;
    private HashMap<String, OrderNew> orders;
    private HashMap<String, OrderCancel> cancelMap;
    private PriceData prevRefPriceData;

    private double stepFactor = 0.0;//0031;
    private Decimal tickSize = Decimal.ONE;
    private double orderSize = 0.01;
    private int bidSteps=1;
    private int askSteps=1;



//    private boolean receivedOrderReceiptBidSide;

} // class

