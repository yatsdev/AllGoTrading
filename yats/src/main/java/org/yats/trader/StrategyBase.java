package org.yats.trader;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.trading.*;

public abstract class StrategyBase implements IConsumePriceDataAndReceipt, IConsumeSettings {

    final Logger log = LoggerFactory.getLogger(StrategyBase.class);

    public static final String SETTING_LOCKED = "lockStrategy";
    public static final String SETTING_STARTED = "startStrategy";
    public static final String SETTING_STRATEGYNAME = "strategyName";

    @Override
    public void onPriceData(PriceData priceData) {
        onPriceDataForStrategy(priceData);
    }

    @Override
    public void onReceipt(Receipt receipt) {
        onReceiptForStrategy(receipt);
    }

    public void shutdown() {
        onShutdown();
    }

    public abstract void onShutdown();
    public abstract void onPriceDataForStrategy(PriceData priceData);
    public abstract void onReceiptForStrategy(Receipt receipt);
    public abstract void onInitStrategy();
    public abstract void onStopStrategy();
    public abstract void onStartStrategy();
    public abstract void onSettingsForStrategy(IProvideProperties p);

    public void init() {
        initialised = true;
        config.set(SETTING_LOCKED, isLocked());
        config.set(SETTING_STARTED, isStarted());
        onInitStrategy();
    }

    @Override
    public UniqueId getConsumerId() { return consumerId; }


    @Override
    public void onSettings(IProvideProperties p) {
        boolean previouslyRunning = isStarted();
        config = p;
        onSettingsForStrategy(p);
        callStartOrStopCallback(previouslyRunning);
    }

    public void stopStrategy() {
        boolean previouslyRunning = isStarted();
        config.set(SETTING_STARTED, "false");
        callStartOrStopCallback(previouslyRunning);
    }

    public void startStrategy() {
        boolean previouslyRunning = isStarted();
        if(!isLocked()) config.set(SETTING_STARTED, "true");
        callStartOrStopCallback(previouslyRunning);
    }

    public void sendReports(IProvideProperties p) {
        reportSender.sendReports(p);
    }

    public void sendReports() {
        reports.set(SETTING_STRATEGYNAME, getName());
        reportSender.sendReports(reports);
    }

    public boolean isStarted() {
        return isLocked() ? false : config.getAsBoolean(SETTING_STARTED, false);
    }

    public boolean isLocked() {
        return config.getAsBoolean(SETTING_LOCKED, false);
    }

    protected boolean isConfigExists(String key) { return config.exists(key); }
    protected String getConfig(String key) {
        return config.get(key);
    }
    protected String getConfig(String key, String defaultValue) {
        return isConfigExists(key) ? config.get(key) : defaultValue;
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


    protected void addTimedCallback(int seconds, IAmCalledBackInTime callback) {
        timedCallbackProvider.addTimedCallback(new TimedCallback(DateTime.now().plusSeconds(seconds), callback));
    }


    public boolean isInitialised() {
        return initialised;
    }



    public void subscribe(String productId)
    {
        log.info("Subscription sent for "+productId+ " by "+getName());
        priceProvider.subscribe(productId, this);
    }

    public void sendNewOrder(OrderNew order)
    {
        if(!isStarted()) {
            log.error("NOT_STARTED! "+ getName() + " tried to send OrderNew although not started: "+order);
            return;
        }
        orderSender.sendOrderNew(order);
    }

    public String getName() {
        return config.get(SETTING_STRATEGYNAME);
    }

    public void sendOrderCancel(OrderCancel order)
    {
        orderSender.sendOrderCancel(order);
    }


    public Product getProductForProductId(String productId) {
        return productProvider.getProductWith(productId);
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

    public Position getValueForProduct(String accountUnitProductId, String productId)
    {
        PositionRequest r = new PositionRequest(getInternalAccount(), productId);
        return positionProvider.getValueForAccountProduct(accountUnitProductId, r);
    }

    public Position getValueForAccount(String account, String accountUnitProductId)
    {
        Position totalValue = new Position(accountUnitProductId, Decimal.ZERO);
        IProvidePosition allPos = positionProvider.getAllPositionsForOneAccount(account);
        for(AccountPosition pos : allPos.getAllPositions()) {
            PositionRequest r = new PositionRequest(account, pos.getProductId());
            Position v = positionProvider.getValueForAccountProduct(accountUnitProductId, r);
            totalValue = totalValue.add(v);
        }
        return totalValue;
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

    public void setTimedCallbackProvider(IProvideTimedCallback timedCallbackProvider) {
        this.timedCallbackProvider = timedCallbackProvider;
    }

    public StrategyBase() {
        consumerId = UniqueId.create();
        initialised = false;
        converter = new RateConverter(new ProductList());
        reports = new PropertiesReader();
        config = new PropertiesReader();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callStartOrStopCallback(boolean previouslyRunning) {
        if(!previouslyRunning && isStarted())
            onStartStrategy();
        else if(previouslyRunning && !isStarted())
            onStopStrategy();
    }


    private String internalAccount;
    private IProvidePriceFeed priceProvider;
    private ISendOrder orderSender;
    private ISendReports reportSender;
    private IProvidePosition positionProvider;
    private IProvideProduct productProvider;
    private IProvideTimedCallback timedCallbackProvider;
    private final UniqueId consumerId;
    private boolean initialised;
    private RateConverter converter;

    private IProvideProperties config;
    PropertiesReader reports;
}