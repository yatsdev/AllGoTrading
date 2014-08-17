package org.yats.trader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trading.*;



public class StrategyRunnerTest {

    /*
        http://www.slf4j.org/faq.html#IllegalAccessError
        Remove from Maven's lib directory all SLF4J JARs with versions before 1.5.6 and their entries in ProjectSettings->Libraries
     */

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
//    final Logger log = LoggerFactory.getLogger(StrategyRunner.class);


    private static String ACCOUNT = "testAccount";

    @Test
    public void canDoSubscriptionForMarketData()
    {
        strategy.init();
        assert (strategyRunner.isProductSubscribed(TestMarketData.TEST_SAP_PID));
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

    @Test
    public void canCalculatePositionSizeAndValue()
    {
        orderConnection.init();
        strategy.sendBuyOrder();
        strategyRunner.waitForProcessingQueues();
        orderConnection.partialFillOrder(2);
        strategyRunner.waitForProcessingQueues();
        Decimal positionSize = strategy.getPositionForProduct(TestMarketData.TEST_SAP_PID);
        assert (positionSize.isEqualTo(Decimal.fromString("2")));
        Position positionValueUSD = strategy.getValueForProduct(TestMarketData.TEST_USD_PID, TestMarketData.TEST_SAP_PID);
        Decimal expected = Decimal.fromString("2")
                .multiply(data1.getLast())
                .multiply(TestMarketData.TEST_EURUSD_LAST);
        assert (positionValueUSD.isSize(expected));
    }



    @BeforeMethod
    public void setUp() {
        data1 = new MarketData(DateTime.now(DateTimeZone.UTC), TestMarketData.TEST_SAP_PID,
                Decimal.fromDouble(10), Decimal.fromDouble(11), Decimal.fromDouble(11),
                Decimal.ONE,Decimal.ONE,Decimal.ONE);

        ProductList productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        rateConverter = new RateConverter(productList);
        rateConverter.onMarketData(TestMarketData.TEST_EURUSD);
        rateConverter.onMarketData(data1);
        positionServer = new PositionServer();
        positionServer.setRateConverter(rateConverter);
        feed = new PriceFeedMock();
        strategy = new StrategyMock();
        strategy.setInternalAccount(ACCOUNT);
        strategyRunner = new StrategyRunner();
        strategyRunner.setRateConverter(rateConverter);
        strategyRunner.addReceiptConsumer(positionServer);
        orderConnection = new OrderConnectionMock(strategyRunner);
        strategy.setPriceProvider(strategyRunner);
        strategy.setOrderSender(strategyRunner);
        strategyRunner.setOrderSender(orderConnection);
        strategyRunner.setPriceFeed(feed);
        strategyRunner.addStrategy(strategy);
        strategyRunner.setProductProvider(productList);

        strategy.setPositionProvider(positionServer);

    }

    @AfterMethod
    public void tearDown() {
        strategyRunner.stop();
    }

    private StrategyRunner strategyRunner;
    private PriceFeedMock feed;
    private OrderConnectionMock orderConnection;
    private StrategyMock strategy;
    private MarketData data1;
    private PositionServer positionServer;
    private RateConverter rateConverter;
    private ProductList productList;


    private static Product testProduct = new Product(TestMarketData.TEST_SAP_PID, TestMarketData.TEST_SAP_SYMBOL, "exchange");

    private class StrategyMock extends StrategyBase {

        public double getPosition() {
            return position;
        }

        public void sendBuyOrder(){
            OrderNew order = OrderNew.create()
                    .withProductId(testProduct.getProductId())
                    .withBookSide(BookSide.BID)
                    .withLimit(Decimal.fromDouble((50)))
                    .withSize(Decimal.fromDouble(5))
                    .withInternalAccount(ACCOUNT)
                    ;

            sendNewOrder(order);
        }

        public void cancelBuyOrder() {
            lastReceipt.getProductId();
            OrderCancel o = OrderCancel.create()
                    .withProductId(lastReceipt.getProductId())
                    .withBookSide(lastReceipt.getBookSide())
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

            position += receipt.getPositionChange().toInt();
            lastReceipt=receipt;
        }

        @Override
        public void onSettings(IProvideProperties p) {

        }

        @Override
        public void init() {
            subscribe(testProduct.getProductId());
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
        public void subscribe(String productId, IConsumeMarketData consumer) {
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
                receipt.setResidualSize(Decimal.ZERO);
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
            receipt.setRejectReason("order productId unknown");
            receiptConsumer.onReceipt(receipt);
        }

        public void partialFillOrder(int fillSize) {
            filledSizeOfOrder = Math.min(filledSizeOfOrder + fillSize, (int) lastOrderNew.getSize().toDouble());
            Receipt receipt = lastOrderNew.createReceiptDefault();
            receipt.setCurrentTradedSize(Decimal.fromDouble(fillSize));
            receipt.setTotalTradedSize(Decimal.fromDouble(filledSizeOfOrder));
            receipt.setEndState(filledSizeOfOrder >= lastOrderNew.getSize().toDouble());
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