package org.yats.messagebus;

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

    public String getTopicCatchAll() {
        return "#";
    }

}
