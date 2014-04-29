package org.yats.trader;

import org.yats.trading.*;

public abstract class StrategyBase implements IConsumeMarketDataAndReceipt {

    @Override
    public abstract void onMarketData(MarketData marketData);

    @Override
    public abstract void onReceipt(Receipt receipt);

    public abstract void init();

    public abstract void shutdown();

    public void subscribe(Product product)
    {
        priceProvider.subscribe(product, this);
    }

    public void sendNewOrder(OrderNew order)
    {
        orderSender.sendOrderNew(order);
    }

    public void sendOrderCancel(OrderCancel order)
    {
        orderSender.sendOrderCancel(order);
    }

    double getPositionForProduct(Product product)
    {
        return positionProvider.getInternalAccountPositionForProduct(getInternalAccount(), product);
    }

    double getProfitForProduct(Product product)
    {
        return profitProvider.getInternalAccountProfitForProduct(getInternalAccount(), product);
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

    public StrategyBase() {
    }

    private String externalAccount;
    private String internalAccount;

    private IProvidePriceFeed priceProvider;
    private ISendOrder orderSender;

    private IProvidePosition positionProvider;
    private IProvideProfit profitProvider;

}