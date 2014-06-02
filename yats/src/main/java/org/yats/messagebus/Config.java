package org.yats.messagebus;

import org.yats.common.PropertiesReader;

public class Config {


    public static final Config DEFAULT = createRealConfig();
    public static final Config DEFAULT_FOR_TESTS = createTestConfig();



    public String getServerIP() {
        return serverIP;
    }

    public String getExchangeMarketData() {
        return exchangeMarketData;
    }

    public String getExchangeReceipts() {
        return exchangeReceipts;
    }

    public String getExchangeSubscription() {
        return exchangeSubscription;
    }

    public String getExchangePositionRequest() {
        return exchangePositionRequest;
    }

    public String getExchangePositionSnapshot() {
        return exchangePositionSnapshot;
    }

    public String getExchangeOrderNew() {
        return exchangeOrderNew;
    }

    public String getExchangeOrderCancel() {
        return exchangeOrderCancel;
    }

    public String getTopicSubscriptions() {
        return topicSubscriptions;
    }

    public String getTopicPositionSnapshot() {
        return topicPositionSnapshot;
    }

    public boolean isListeningForReceipts() {
        return listeningForReceipts;
    }

    public boolean isStorePositionsToDisk() {
        return storePositionsToDisk;
    }

    public String getPositionFilename() {
        return positionFilename;
    }

    public String getTopicCatchAll() {
        return "#";
    }

    public void setStorePositionsToDisk(boolean storePositions) {
        this.storePositionsToDisk = storePositions;
    }

    public void setListeningForReceipts(boolean listeningToReceipts) {
        this.listeningForReceipts = listeningToReceipts;
    }

    public static Config fromProperties(PropertiesReader p) {
        Config c = new Config();
        c.serverIP = p.get("serverIP","127.0.0.1");
        c.exchangeMarketData = p.get("exchangeMarketData","marketdata");
        c.exchangeReceipts = p.get("exchangeReceipts","receipts");
        c.exchangeSubscription = p.get("exchangeSubscription","subscriptions");
        c.exchangePositionRequest = p.get("exchangePositionRequest","positionrequests");
        c.exchangePositionSnapshot = p.get("exchangePositionSnapshot","positionsnapshots");
        c.exchangeOrderNew = p.get("exchangeOrderNew","orderNew");
        c.exchangeOrderCancel = p.get("exchangeOrderCancel","orderCancel");
        c.topicSubscriptions = p.get("topicSubscriptions","subscriptions");
        c.topicPositionSnapshot = p.get("topicPositionSnapshot","topicPositionSnapshot");
        c.listeningForReceipts = p.getAsBoolean("listeningForReceipts", false);
        c.storePositionsToDisk = p.getAsBoolean("storePositionsToDisk", false);
        c.positionFilename = p.get("positionFilename", "PositionDefault.csv");
        return c;
    }

    private static Config createRealConfig() {
        Config config = new Config();
        config.initRealDefault();
        return config;
    }


    private void initRealDefault() {
        serverIP = "127.0.0.1";
        exchangeMarketData = "marketdata";
        exchangeReceipts = "receipts";
        exchangeSubscription = "subscriptions";
        exchangePositionRequest = "positionrequests";
        exchangePositionSnapshot = "positionsnapshots";
        exchangeOrderNew = "orderNew";
        exchangeOrderCancel = "orderCancel";
        topicSubscriptions = "subscriptions";
        topicPositionSnapshot = "topicPositionSnapshot";
        listeningForReceipts = false;
        storePositionsToDisk = false;
        positionFilename = "Position.csv";
    }

    private static Config createTestConfig() {
        Config config = new Config();
        config.initTestDefault();
        return config;
    }

    private void initTestDefault() {
        serverIP = "127.0.0.1";
        exchangeMarketData = "marketdataTest";
        exchangeReceipts = "receiptsTest";
        exchangeSubscription = "subscriptionsTest";
        exchangePositionRequest = "positionrequestsTest";
        exchangePositionSnapshot = "positionsnapshotsTest";
        exchangeOrderNew = "orderNewTest";
        exchangeOrderCancel = "orderCancelTest";
        topicSubscriptions = "subscriptionsTest";
        topicPositionSnapshot = "topicPositionSnapshotTest";
        listeningForReceipts = false;
        storePositionsToDisk = false;
        positionFilename = "PositionTest.csv";
    }

    public Config() {
    }

    private String serverIP;
    private String exchangeMarketData;
    private String exchangeReceipts;
    private String exchangeSubscription;
    private String exchangePositionRequest;
    private String exchangePositionSnapshot;
    private String exchangeOrderNew;
    private String exchangeOrderCancel;
    private String topicSubscriptions;
    private String topicPositionSnapshot;
    private boolean listeningForReceipts;
    private boolean storePositionsToDisk;
    private String positionFilename;


}
