package org.yats.trader;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.*;

public abstract class StrategyBase implements IConsumeMarketDataAndReceipt {


    @Override
    public abstract void onMarketData(MarketData marketData);

    @Override
    public UniqueId getConsumerId() { return consumerId; }

    @Override
    public abstract void onReceipt(Receipt receipt);

    public abstract void init();

    public abstract void shutdown();

    public void subscribe(String productId)
    {
        priceProvider.subscribe(productId, this);
    }

    public void sendNewOrder(OrderNew order)
    {
        orderSender.sendOrderNew(order);
    }

    public void sendOrderCancel(OrderCancel order)
    {
        orderSender.sendOrderCancel(order);
    }

    public Product getProductForProductId(String productId) {
        return productProvider.getProductForProductId(productId);
    }

    Decimal getPositionForProduct(String productId)
    {
        return positionProvider.getInternalAccountPositionForProduct(getInternalAccount(), productId);
    }

    Decimal getProfitForProduct(String productId)
    {
        return profitProvider.getInternalAccountProfitForProduct(getInternalAccount(), productId);
    }

    public String getExternalAccount() {
        return externalAccount;
    }

    public void setPriceProvider(IProvidePriceFeed priceProvider) {
        this.priceProvider = priceProvider;
    }

    public void setOrderSender(ISendOrder orderSender) {
        this.orderSender = orderSender;
    }

    public void setPositionProvider(IProvidePosition positionProvider) {
        this.positionProvider = positionProvider;
    }

    public void setProfitProvider(IProvideProfit profitProvider) {
        this.profitProvider = profitProvider;
    }

    public void setExternalAccount(String internalAccount) {
        this.externalAccount = internalAccount;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public void setInternalAccount(String internalAccount) {
        this.internalAccount = internalAccount;
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public StrategyBase() {
        consumerId = UniqueId.create();
    }


    private String externalAccount;
    private String internalAccount;

    private IProvidePriceFeed priceProvider;
    private ISendOrder orderSender;

    private IProvidePosition positionProvider;
    private IProvideProfit profitProvider;
    private IProvideProduct productProvider;

    private final UniqueId consumerId;


}