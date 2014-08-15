package org.yats.connectivity.matching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.UniqueId;
import org.yats.trading.*;

import java.util.concurrent.ConcurrentHashMap;

public class InternalMarket implements IProvidePriceFeed,ISendOrder,IConsumeMarketDataAndReceipt {

    final Logger log = LoggerFactory.getLogger(InternalMarket.class);

    @Override
    public void subscribe(String productId, IConsumeMarketData _consumer) {
        priceConsumer = _consumer;
        if(!isProductValid(productId)) return;
        createOrderBookForProductId(productId);
        log.debug("Subscription for " + productId + ". OrderBook created.");
    }

    @Override
    public void sendOrderNew(OrderNew order) {
        String productId = order.getProductId();
        if(!isProductValid(productId)) return;
        createOrderBookForProductId(productId);

        String orderId = order.getOrderIdString();
        if(cancelOrderMap.containsKey(orderId)) {
            log.debug("received OrderNew was already canceled: "+orderId);
            cancelOrderMap.remove(orderId);
        } else {
            log.debug("matching OrderNew "+order.toString());
            orderBooks.get(productId).match(order);
        }
        log.debug("#orders in book: "+orderBooks.get(productId).getOrderCount());
    }

    @Override
    public void sendOrderCancel(OrderCancel order) {
        String productId = order.getProductId();
        if(!isProductValid(productId)) return;
        createOrderBookForProductId(productId);
        log.debug("canceling order "+order.toString());
        LimitOrderBook book = orderBooks.get(productId);

        if(book.isOrderInBooks(order.getOrderId()))
            book.cancel(order.getOrderId());
        else
            confirmAndStoreCancelForNotYetArrivedOrderNew(order);

        log.debug("#orders in book: "+orderBooks.get(productId).getOrderCount());
    }

    @Override
    public void onMarketData(MarketData marketData) {
        if(priceConsumer==null) {
            System.out.println("why null?");
        }
        priceConsumer.onMarketData(marketData);
    }

    @Override
    public void onReceipt(Receipt receipt) {
        receipt.setExternalAccount(externalAccount);
        receiptConsumer.onReceipt(receipt);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    public void setReceiptConsumer(IConsumeReceipt receiptConsumer) {
        this.receiptConsumer = receiptConsumer;
    }

    public void setPriceConsumer(IConsumeMarketData priceConsumer) {
        this.priceConsumer = priceConsumer;
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public InternalMarket(String _externalAccount, String _marketName) {
        externalAccount = _externalAccount;
        marketName = _marketName;
        orderBooks = new ConcurrentHashMap<String, LimitOrderBook>();
        cancelOrderMap = new ConcurrentHashMap<String, OrderCancel>();
        priceConsumer=null;
        receiptConsumer=null;
        productProvider=null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isProductValid(String productId) {
        if(!productProvider.isProductIdExisting(productId)) return false;
        Product p = productProvider.getProductForProductId(productId);
        if(!p.hasExchange(marketName)) return false;
        return true;
    }

    private void rejectUnknownCancelOrder(OrderCancel order) {
        Receipt r = order.createReceiptDefault().withEndState(true).withRejectReason("Unknown order.");
        onReceipt(r);
    }

    private void confirmAndStoreCancelForNotYetArrivedOrderNew(OrderCancel order) {
        log.info("remembering OrderCancel for later: "+order.getOrderIdString());
        rememberCancelOrder(order);
        Receipt r = order.createReceiptDefault().withEndState(true);
        onReceipt(r);
    }

    private void rememberCancelOrder(OrderCancel order) {
        if(!cancelOrderMap.containsKey(order.getOrderIdString())) {
            cancelOrderMap.put(order.getOrderIdString(), order);
        }
    }

    private void createOrderBookForProductId(String productId) {
        if(!orderBooks.containsKey(productId)) {
            orderBooks.put(productId, new LimitOrderBook(productId, this));
        }
    }

    private ConcurrentHashMap<String, OrderCancel> cancelOrderMap;
    private ConcurrentHashMap<String, LimitOrderBook> orderBooks;
    private IConsumeMarketData priceConsumer;
    private IConsumeReceipt receiptConsumer;
    private IProvideProduct productProvider;
    private String externalAccount;
    private String marketName;





}
