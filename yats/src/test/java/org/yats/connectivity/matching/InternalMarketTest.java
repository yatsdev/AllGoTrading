package org.yats.connectivity.matching;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.messagebus.messages.OrderNewMsg;
import org.yats.trading.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InternalMarketTest implements IConsumeReceipt, IConsumePriceData {

    @Test
    public void canInsertAndCancel() {
        market.sendOrderNew(bid133);
        market.sendOrderNew(ask135);
        assert(mdCounter==2);
        assert(lastPriceData.hasFrontRow(BookSide.BID, bid133.getAsRow()));
    }

    @Test
    public void canHandleMultipleNonCrossingOrders() {
        OrderNew newBid = bid133;
        sendMultipleOrders(newBid, 99);
        assert(mdCounter==99);
        OrderNew newAsk = ask135;
        sendMultipleOrders(newAsk, 99);
        assert(mdCounter==198);
        assert(lastPriceData.hasFrontRow(BookSide.BID, new BookRow(Decimal.ONE, Decimal.fromString("133.99"))));
        assert(lastPriceData.hasFrontRow(BookSide.ASK, new BookRow(Decimal.ONE, Decimal.fromString("134.01"))));
    }

    @Test
    public void canProduceProductCounterReceipts() {
        OrderNew makerBid = createCopy(bid133);
        market.sendOrderNew(makerBid);
        OrderNew crossingAsk = createCopy(ask135).withLimit(bid133.getLimit());
        market.sendOrderNew(crossingAsk);
        assert(mdCounter==2);
        assert(receiptCounter==3);
        for(Receipt r : receiptUnitsList) {
            assert(r.getCurrentTradedSize().isEqualTo(bid133.getLimit()));
        }

    }

    @Test
    public void canHandleMultipleCrossingOrders() {
        OrderNew newBid = createCopy(bid133);
        sendMultipleOrders(newBid, 99);
        assert(mdCounter==99);
        OrderNew newAsk = createCopy(ask135).withLimit(bid133.getLimit().add(Decimal.ONE));
        sendMultipleOrders(newAsk, 99);
        assert(mdCounter==198);
        assert(lastPriceData.isBookSideEmpty(BookSide.BID)); //hasFrontRow(BookSide.BID, new BookRow(Decimal.ONE, Decimal.fromString("133.01"))));
        assert(lastPriceData.isBookSideEmpty(BookSide.ASK)); //hasFrontRow(BookSide.BID, new BookRow(Decimal.ONE, Decimal.fromString("133.01"))));
//        assert(lastMarketData.hasFrontRow(BookSide.ASK, new BookRow(Decimal.ONE, Decimal.fromString("134.00"))));
    }

    @Test
    public void canHandleOneOrderFillingMultipleOrders() {
        OrderNew newBid = createCopy(bid133);
        sendMultipleOrders(newBid, 99);
        assert(mdCounter==99);
        assert(receiptCounter==99);
        OrderNew newAsk = createCopy(ask135)
                .withLimit(bid133.getLimit())
                .withSize(Decimal.fromString("98.5"));
        market.sendOrderNew(newAsk);
        assert(mdCounter==100);
        assert(receiptCounter==3*99);
        assert(lastPriceData.hasFrontRow(BookSide.BID, new BookRow(Decimal.fromString("0.5"), Decimal.fromString("133.01"))));
        assert(lastPriceData.isBookSideEmpty(BookSide.ASK));
    }

    @Test
    public void canCancelMultipleOrdersTowardsFrontRow() {
        OrderNew newBid = createCopy(bid133);
        sendMultipleOrders(newBid, 99);
        assert(receiptCounter==99);
        assert(mdCounter==99);
        List<Receipt> snapshot = new ArrayList<Receipt>();
        snapshot.addAll(receiptList);

        int counter=1;
        for(Receipt r : snapshot) {
            OrderCancel c = r.createOrderCancel(r);
            market.sendOrderCancel(c);
            Decimal expected = bid133.getLimit().add(Decimal.fromString("0.99"));
            if(counter<99) assert(lastPriceData.getBid().isEqualTo(expected));
            if(counter==99) assert(lastPriceData.isBookSideEmpty(BookSide.BID));
            counter++;
        }
        assert(receiptCounter==2*99);
        assert(mdCounter==99+99);
    }

    @Test
    public void canCancelMultipleOrdersStartingFrontRow() {
        OrderNew newAsk = createCopy(ask135);
        sendMultipleOrders(newAsk, 99);
        assert(receiptCounter==99);
        assert(mdCounter==99);
        List<Receipt> snapshot = new ArrayList<Receipt>();
        snapshot.addAll(receiptList);
        Collections.reverse(snapshot);

        Decimal expected = ask135.getLimit().subtract(Decimal.fromString("0.98"));
        int counter=1;
        for(Receipt r : snapshot) {
            OrderCancel c = r.createOrderCancel(r);
            market.sendOrderCancel(c);
            if(counter<99) assert(lastPriceData.getAsk().isEqualTo(expected));
            if(counter==99) assert(lastPriceData.isBookSideEmpty(BookSide.ASK));
            counter++;
            expected=expected.add(Decimal.CENT);
        }
        assert(receiptCounter==2*99);
        assert(mdCounter==99+99);
    }


    private OrderNew createCopy(OrderNew order) {
        return OrderNewMsg.createFromOrderNew(order).toOrderNew();
    }

    private void sendMultipleOrders(OrderNew order, int amount) {
        for(int i = 0; i<amount; i++) {
            order.withOrderId(UniqueId.create());
            double step = order.getBookSide().toDirection();
            order.withLimit(order.getLimit().add(Decimal.fromDouble(step/100.0)));
            market.sendOrderNew(order);
        }
    }

    @BeforeMethod
    public void setUp() {
        ProductList products = ProductList.createFromFile(ProductList.PATH);
        market = new InternalMarket(ProductTest.TEST_EXTACCOUNT, ProductTest.TEST_MARKET);
        market.setReceiptConsumer(this);
        market.setProductProvider(products);
        market.subscribe(ProductTest.PRODUCT_TEST.getProductId(), this);

        bid133 = OrderNew.create()
                .withProductId(ProductTest.PRODUCT_TEST.getProductId())
                .withInternalAccount(ProductTest.TEST_ACCOUNT)
                .withBookSide(BookSide.BID)
                .withLimit(Decimal.fromString("133"))
                .withSize(Decimal.fromString("1"))
        ;
        ask135 = OrderNew.create()
                .withProductId(ProductTest.PRODUCT_TEST.getProductId())
                .withInternalAccount(ProductTest.TEST_ACCOUNT)
                .withBookSide(BookSide.ASK)
                .withLimit(Decimal.fromString("135"))
                .withSize(Decimal.fromString("1"))
        ;

        mdCounter=0;
        receiptCounter=0;
        receiptList=new ArrayList<Receipt>();
        receiptUnitsList=new ArrayList<Receipt>();
        mdList=new ArrayList<PriceData>();
    }

    @Override
    public void onReceipt(Receipt receipt) {
        receiptCounter++;
        lastReceipt=receipt;
        if(receipt.isForProduct(ProductTest.PRODUCT_TEST))
            receiptList.add(receipt);
        if(receipt.hasProductId(ProductTest.PRODUCT_TEST.getUnitId()))
            receiptUnitsList.add(receipt);

//        System.out.println(receipt);
    }

    @Override
    public void onPriceData(PriceData priceData) {
        mdCounter++;
        lastPriceData = priceData;
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    OrderNew bid133;
    OrderNew ask135;
    InternalMarket market;
    int mdCounter;
    int receiptCounter;
    PriceData lastPriceData;
    Receipt lastReceipt;
    List<Receipt> receiptList;
    List<Receipt> receiptUnitsList;
    List<PriceData> mdList;


} // class
