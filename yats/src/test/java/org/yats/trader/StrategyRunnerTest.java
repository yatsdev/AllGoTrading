package org.yats.trader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.trading.*;

public class StrategyRunnerTest {

    /*
        http://www.slf4j.org/faq.html#IllegalAccessError
        Remove from Maven's lib directory all SLF4J JARs with versions before 1.5.6 and their entries in ProjectSettings->Libraries
     */

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);


    private static String SECURITY_ID_SAP ="SAP";
    private static Product testProduct = new Product(SECURITY_ID_SAP, "", "");

    @Test
    public void canDoSubscriptionForMarketData()
    {
        strategy.init();
        assert (strategyRunner.isProductSubscribed(SECURITY_ID_SAP));
    }

    @Test
    public void canReceiveMarketDataAndSendToStrategy()
    {
        assert (strategy.marketDataReceived == 0);
        strategy.init();
        feed.sendMarketData();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.marketDataReceived == 1);
    }

    @Test
    public void canSendOrderAndReceivesReceipt()
    {
        strategy.sendBuyOrder();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 1);
        assert (strategy.getPosition() == 0);
    }

    @Test
    public void canSendMarketCrossingOrderAndReceivesFilledReceipt()
    {
        orderConnection.setFillOrderImmediately();
        strategy.sendBuyOrder();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 0);
        assert (strategy.getPosition() == 5);
    }

    @Test
    public void canCancelOrder()
    {
        strategy.sendBuyOrder();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 1);
        strategy.cancelBuyOrder();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 0);
        assert (strategy.getPosition() == 0);
    }

    @Test
    public void canProcessPartialFill()
    {
        orderConnection.init();
        strategy.sendBuyOrder();
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 1);
        orderConnection.partialFillOrder(2);
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 1);
        assert (strategy.getPosition() == 2);
        orderConnection.partialFillOrder(3);
        strategyRunner.waitForProcessingQueues();
        assert (strategy.getNumberOfOrdersInMarket() == 0);
        assert (strategy.getPosition() == 5);
    }


    @BeforeMethod
    public void setUp() {
        feed = new PriceFeedMock();
        strategy = new StrategyMock();
        strategyRunner = new StrategyRunner();
        orderConnection = new OrderConnectionMock(strategyRunner);
        strategy.setPriceProvider(strategyRunner);
        strategy.setOrderSender(strategyRunner);
        strategyRunner.setOrderSender(orderConnection);
        strategyRunner.setPriceFeed(feed);
        strategyRunner.addStrategy(strategy);
        data1 = new MarketData(DateTime.now(DateTimeZone.UTC), SECURITY_ID_SAP,10,11,1,1);
    }


    private StrategyRunner strategyRunner;
    private PriceFeedMock feed;
    private OrderConnectionMock orderConnection;
    private StrategyMock strategy;
    private MarketData data1;

    private class StrategyMock extends StrategyBase {

        public double getPosition() {
            return position;
        }

        public void sendBuyOrder(){
            OrderNew order = OrderNew.create()
                    .withProduct(testProduct)
                    .withBookSide(BookSide.BID)
                    .withLimit(50)
                    .withSize(5);
            sendNewOrder(order);
        }

        public void cancelBuyOrder() {
            lastReceipt.getProduct().getId();
            OrderCancel o = OrderCancel.create()
                    .withProduct(lastReceipt.getProduct())
                    .withBookSide(lastReceipt.getBookSide())
                    .withExternalAccount(lastReceipt.getExternalAccount())
                    .withOrderId(lastReceipt.getOrderId())
                    ;
            sendOrderCancel(o);
        }

        int getNumberOfOrdersInMarket() {
            return numberOfOrderInMarket;
        }


        @Override
        public void onMarketData(MarketData marketData) {
            marketDataReceived++;
        }

        @Override
        public void onReceipt(Receipt receipt) {
            if(!receipt.isForSameOrderAs(lastReceipt)) numberOfOrderInMarket++;
            if(receipt.isEndState()) numberOfOrderInMarket--;

            position += receipt.getPositionChange();
            lastReceipt=receipt;
        }

        @Override
        public void init() {
            subscribe(testProduct);
        }

        @Override
        public void shutdown() {}

        private StrategyMock() {
            marketDataReceived=0;
            position = 0;
            lastReceipt = Receipt.NULL;
        }

        private double position;
        private int marketDataReceived;
        private int numberOfOrderInMarket;
        private Receipt lastReceipt;

    }

    private class PriceFeedMock implements IProvidePriceFeed {
        @Override
        public void subscribe(Product p, IConsumeMarketData consumer) {
            this.consumer=consumer;
        }
        IConsumeMarketData consumer;

        public void sendMarketData() {
            consumer.onMarketData(data1);
        }
    }

    private class OrderConnectionMock implements ISendOrder {


        @Override
        public void sendOrderNew(OrderNew orderNew) {
            lastOrderNew =orderNew;
            Receipt receipt = orderNew.createReceiptDefault();
            if(fillOrderImmediately) {
                receipt.setTotalTradedSize(orderNew.getSize());
                receipt.setCurrentTradedSize(orderNew.getSize());
                receipt.setResidualSize(0);
                receipt.setEndState(true);

            }
            receiptConsumer.onReceipt(receipt);
        }

        @Override
        public void sendOrderCancel(OrderCancel orderCancel) {
            if(!orderCancel.isSameOrderId(lastOrderNew)) {rejectCancelForUnknownOrder(orderCancel); return; }
            Receipt receipt = lastOrderNew.createReceiptDefault();
            receipt.setEndState(true);
            receiptConsumer.onReceipt(receipt);
        }

        private void rejectCancelForUnknownOrder(OrderCancel orderCancel) {
            Receipt receipt = orderCancel.createReceiptDefault();
            receipt.setEndState(true);
            receipt.setRejectReason("order id unknown");
            receiptConsumer.onReceipt(receipt);
        }

        public void partialFillOrder(int fillSize) {
            filledSizeOfOrder = Math.min(filledSizeOfOrder + fillSize, (int) lastOrderNew.getSize());
            Receipt receipt = lastOrderNew.createReceiptDefault();
            receipt.setCurrentTradedSize(fillSize);
            receipt.setTotalTradedSize(filledSizeOfOrder);
            receipt.setEndState(filledSizeOfOrder>=lastOrderNew.getSize()?true:false);
            receiptConsumer.onReceipt(receipt);
        }

        public void init() {
            filledSizeOfOrder=0;
            fillOrderImmediately = false;
        }

        void setFillOrderImmediately(){
            fillOrderImmediately = true;
        }

        private OrderConnectionMock(IConsumeReceipt receiptConsumer) {
            this.receiptConsumer = receiptConsumer;
            init();
        }

        private IConsumeReceipt receiptConsumer;
        private boolean fillOrderImmediately;
        private OrderNew lastOrderNew;
        private int filledSizeOfOrder;

    }

} // class
