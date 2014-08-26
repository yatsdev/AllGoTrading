package org.yats.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.trading.*;

public abstract class StrategyBase implements IConsumeMarketDataAndReceipt, IConsumeSettings {

    final Logger log = LoggerFactory.getLogger(StrategyBase.class);


    @Override
    public abstract void onMarketData(MarketData marketData);

    @Override
    public UniqueId getConsumerId() { return consumerId; }

    @Override
    public abstract void onReceipt(Receipt receipt);

    @Override
    public abstract void onSettings(IProvideProperties p);


    protected boolean isConfigExists(String key) { return config.exists(key); }
    protected String getConfig(String key) {
        return config.get(key);
    }

    protected double getConfigAsDouble(String key) {
        return new Decimal(config.get(key)).toDouble();
    }
    protected int getConfigAsInt(String key) {
        return new Decimal(config.get(key)).toInt();
    }

    protected Decimal getConfigAsDecimal(String key) {
        return new Decimal(config.get(key));
    }
    protected boolean getConfigAsBoolean(String key) { return config.getAsBoolean(key); }
    public void setConfig(IProvideProperties config) {
        this.config = config;
    }

    public void setConfigItem(String key, String value) {
        config.set(key, value);
    }

    public void sendConfig() {

    }

    public void init() {
        initialised = true;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public abstract void shutdown();

    public void subscribe(String productId)
    {
        log.info("Subscription sent for "+productId+ " by "+this.getClass().getSimpleName());
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

    public void sendReports(IProvideProperties p) {
        reportSender.sendReports(p);
    }

    public Product getProductForProductId(String productId) {
        return productProvider.getProductForProductId(productId);
    }

    public Decimal getPositionForProduct(String productId)
    {
        PositionRequest p = new PositionRequest(getInternalAccount(), productId);
        return positionProvider.getAccountPosition(p).getSize();
    }

    public boolean isConversionAvailable(String targetProductId, String productId) {
        try {
            PositionRequest r = new PositionRequest(getInternalAccount(), productId);
            positionProvider.getValueForAccountProduct(targetProductId, r);
            return true;
        } catch(TradingExceptions.RateConverterException e) {
            return false;
        }
    }

    public Position getValueForProduct(String targetProductId, String productId)
    {
        PositionRequest r = new PositionRequest(getInternalAccount(), productId);
        return positionProvider.getValueForAccountProduct(targetProductId, r);
    }

    public PropertiesReader getReports() {
        return reports;
    }

//    public Decimal getProfitForProduct(String productId)
//    {
//        return positionProvider.getValueForAccountProduct(converter, new PositionRequest(getInternalAccount(), productId));
//    }

    public void setPriceProvider(IProvidePriceFeed priceProvider) {
        this.priceProvider = priceProvider;
    }

    public void setOrderSender(ISendOrder orderSender) {
        this.orderSender = orderSender;
    }

    public void setReportSender(ISendReports reportSender) {
        this.reportSender = reportSender;
    }

    public void setPositionProvider(IProvidePosition positionProvider) {
        this.positionProvider = positionProvider;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public void setInternalAccount(String a) {
        this.internalAccount = a;
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public void setName(String name) {
        this.name = name;
        reports.set("strategyName", name);
    }


    public StrategyBase() {
        consumerId = UniqueId.create();
        initialised = false;
        converter = new RateConverter(new ProductList());
        reports = new PropertiesReader();
        setName("unnamedStrategy");
    }


    private String internalAccount;

    private IProvidePriceFeed priceProvider;
    private ISendOrder orderSender;
    private ISendReports reportSender;

    private IProvidePosition positionProvider;
    private IProvideProduct productProvider;

    private final UniqueId consumerId;

    private IProvideProperties config;
    private boolean initialised;
    private RateConverter converter;

    PropertiesReader reports;
    private String name;
}