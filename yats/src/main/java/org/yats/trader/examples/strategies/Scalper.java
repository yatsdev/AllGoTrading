package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.HashMap;

public class Scalper extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(Scalper.class);

    @Override
    public void onPriceData(PriceData priceData)
    {
        if(!isInitialised()) return;
        if(!priceData.hasProductId(tradeProductId)) return;
        if(!startPrice.equals(PriceData.NULL)) return;
        if(shuttingDown) return;
        startPrice = priceData;

        sendBidRelativeTo(startPrice.getBid());
        Decimal step = getStepSize(startPrice.getAsk());
        sendAskRelativeTo(startPrice.getAsk().add(step));
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
        if(!isInitialised()) return;
        if(shuttingDown) return;
        if(receipt.getRejectReason().length()>0) {
            log.error("Received rejection! Stopping for now!");
            shutdown();
            System.exit(-1);
        }
        if(!receipt.hasProductId(tradeProductId)){
//            log.error("Received receipt for unknown product: " + receipt);
            return;
        }

        position = receipt.getPositionChangeOfBase().add(position);
        log.info("position(strategy)="+position);
        log.info("position(server)="+getPositionForProduct(tradeProductId));
        try {
            if (isConversionAvailable(ProductList.USD_PID, tradeProductId))
                log.info("positionValueUSD(server)=" + getValueForProduct(ProductList.USD_PID, tradeProductId));
            log.info("positionValueEUR(server)=" + getValueForProduct(ProductList.EUR_PID, tradeProductId));
            log.info("accountValueEUR="+getValueForAccount(getInternalAccount(), ProductList.EUR_PID));
            log.info("accountValueUSD="+getValueForAccount(getInternalAccount(), ProductList.USD_PID));
        } catch(TradingExceptions.RateConverterException e) {
            log.error("Can not calculate position value: "+e.getMessage());
        }
        log.debug("Received receipt: " + receipt);

        if(receipt.isEndState()) {
            orders.remove(receipt.getOrderId().toString());
            if(receipt.isTrade())    {
                sendAskRelativeTo(receipt.getPrice());
                sendBidRelativeTo(receipt.getPrice());
            }
        }

    }

    @Override
    public void onSettings(IProvideProperties p) {

    }

    @Override
    public void init()
    {
        super.init();
        setInternalAccount(getConfig("internalAccount"));
        tradeProductId = getConfig("tradeProductId");
        subscribe(tradeProductId);
        subscribe("OANDA_EURUSD");
        position = getPositionForProduct(tradeProductId);
        log.info("position="+position);
        tickSize =getConfigAsDecimal("tickSize");

        if(isConfigExists("stepFactor")) stepFactor=getConfigAsDecimal("stepFactor");
        if(isConfigExists("stepInTicks")) {stepInTicks=getConfigAsDecimal("stepInTicks"); stepFactor=Decimal.ZERO; }
        orderSize=getConfigAsDouble("orderSize");
        if(isConfigExists("shortingAllowed")) shortingAllowed = getConfigAsBoolean("shortingAllowed");
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        cancelOrders();
    }


    private void sendBidRelativeTo(Decimal price) {
        double bidMarket=price.toDouble();
        Decimal bidPrice = Decimal.fromDouble(bidMarket);
        bidPrice = bidPrice.subtract(getStepSize(bidPrice));
        if(!orderExists(BookSide.BID, bidPrice))
            sendOrder(BookSide.BID, bidPrice);
    }

    private void sendAskRelativeTo(Decimal price) {

        if(!shortingAllowed && position.isLessThan(Decimal.fromDouble(orderSize))) {
            log.info("Can not sell. Position less than orderSize. positionSize="+position);
            return;
        }
        double askMarket=price.toDouble();
        Decimal askPrice = Decimal.fromDouble(askMarket);
        askPrice = askPrice.add(getStepSize(askPrice));
        if(!orderExists(BookSide.ASK, askPrice))
            sendOrder(BookSide.ASK, askPrice);
    }

    private Decimal getStepSize(Decimal price) {
        Decimal stepSize = tickSize;
        if(stepFactor.isGreaterThan(Decimal.ZERO))
            stepSize = Decimal.max(tickSize, price.multiply(stepFactor).roundToTickSize(tickSize));
        else
            stepSize = stepInTicks.multiply(tickSize);
        return stepSize;
    }

    private boolean orderExists(BookSide side, Decimal price) {
        for(OrderNew order : orders.values()) {
            boolean sameSide = order.getBookSide().equals(side);
            boolean samePrice = order.getLimit().isEqualTo(price);
            if(samePrice && sameSide) return true;
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

    private void cancelOrders() {
        for(OrderNew order : orders.values()) {
            OrderCancel o = order.createCancelOrder();
            sendOrderCancel(o);
        }
    }


    public Scalper() {
        super();
        startPrice = PriceData.NULL;
        shuttingDown=false;
        position = Decimal.ZERO;
        orders = new HashMap<String, OrderNew>();
    }

    private PriceData startPrice;


    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private HashMap<String, OrderNew> orders;

    private Decimal stepInTicks = Decimal.ONE;
    private Decimal stepFactor = Decimal.ZERO;
    private Decimal tickSize = Decimal.CENT;
    private double orderSize = 0.01;
    private boolean shortingAllowed = false;

//    private boolean receivedOrderReceiptBidSide;

} // class
