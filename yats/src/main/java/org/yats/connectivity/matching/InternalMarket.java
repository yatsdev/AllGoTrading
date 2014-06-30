package org.yats.connectivity.matching;

import org.yats.common.UniqueId;
import org.yats.trading.*;

import java.util.concurrent.ConcurrentHashMap;

public class InternalMarket implements IProvidePriceFeed,ISendOrder,IConsumeMarketDataAndReceipt {


    private ProductList productProvider;

    @Override
    public void subscribe(String productId, IConsumeMarketData _consumer) {
        priceConsumer = _consumer;
        createOrderBookForProductId(productId);
    }

    @Override
    public void sendOrderNew(OrderNew order) {
        String productId = order.getProductId();
        createOrderBookForProductId(productId);
        orderBooks.get(productId).match(order);
    }

    @Override
    public void sendOrderCancel(OrderCancel order) {
        String productId = order.getProductId();
        createOrderBookForProductId(productId);
        orderBooks.get(productId).cancel(order);
    }


    @Override
    public void onMarketData(MarketData marketData) {
        priceConsumer.onMarketData(marketData);
    }

    @Override
    public void onReceipt(Receipt receipt) {
        receiptConsumer.onReceipt(receipt);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    public void setReceiptConsumer(IConsumeReceipt receiptConsumer) {
        this.receiptConsumer = receiptConsumer;
    }


    private void createOrderBookForProductId(String productId) {
        if(!orderBooks.containsKey(productId)) {
            orderBooks.put(productId, new LimitOrderBook(this));
        }
    }

    private ConcurrentHashMap<String, LimitOrderBook> orderBooks;
    private IConsumeMarketData priceConsumer;
    private IConsumeReceipt receiptConsumer;


    public static InternalMarket createFromConfigFile(String configFilename) {
        return null;
    }

    public void setProductProvider(ProductList productProvider) {
        this.productProvider = productProvider;
    }
}
