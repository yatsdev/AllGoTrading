package org.yats.messagebus;

import org.yats.common.FileTool;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;

public class Config {


//    public static final Config DEFAULT = createRealConfig();
    public static final Config DEFAULT_FOR_TESTS = createTestConfig();



    public String getServerIP() {
        return properties.get("serverIP");
    }

    public String getExchangePriceData() {
        return properties.get("exchangeMarketData");
    }

    public String getExchangeReceipts() {
        return properties.get("exchangeReceipts");
    }

    public String getExchangeSubscription() {
        return properties.get("exchangeSubscription");
    }

    public String getExchangePositionRequest() {
        return properties.get("exchangePositionRequest");
    }

    public String getExchangePositionSnapshot() {
        return properties.get("exchangePositionSnapshot");
    }

    public String getExchangeKeyValueFromStrategy() {
        return properties.get("exchangeKeyValueFromStrategy");
    }
    public String getExchangeKeyValueToStrategy() {
        return properties.get("exchangeKeyValueToStrategy");
    }

    public String getExchangeOrderNew() {
        return properties.get("exchangeOrderNew");
    }

    public String getExchangeOrderCancel() {
        return properties.get("exchangeOrderCancel");
    }

    public String getTopicSubscriptions() {
        return properties.get("topicSubscriptions");
    }

    public String getTopicPositionSnapshot() {
        return properties.get("topicPositionSnapshot");
    }

    public boolean isListeningForReceipts() {
        return properties.getAsBoolean("listeningForReceipts",false);
    }

    public boolean isStorePositionsToDisk() {
        return properties.getAsBoolean("storePositionsToDisk",false);
    }

    public boolean isReceiverForSettings() { return properties.getAsBoolean("receiveSettings", false); }
    public boolean isReceiverForReports() { return properties.getAsBoolean("receiveReports", false); }

    public String getPositionFilename() {
        return properties.get("positionFilename");
    }

    public String getTopicCatchAll() {
        return "#";
    }

    public void setStorePositionsToDisk(boolean storePositions) {
        properties.set("storePositionsToDisk", storePositions);
    }

    public void setListeningForReceipts(boolean listeningToReceipts) {
        properties.set("listeningForReceipts", listeningToReceipts);
    }

    public static Config fromProperties(IProvideProperties p) {

        Config c = new Config();
        c.setProperties(p);
//        c.serverIP = p.get("serverIP","127.0.0.1");
//        c.exchangeMarketData = p.get("exchangeMarketData","marketdata");
//        c.exchangeReceipts = p.get("exchangeReceipts","receipts");
//        c.exchangeSubscription = p.get("exchangeSubscription","subscriptions");
//        c.exchangePositionRequest = p.get("exchangePositionRequest","positionrequests");
//        c.exchangePositionSnapshot = p.get("exchangePositionSnapshot","positionsnapshots");
//        c.exchangeOrderNew = p.get("exchangeOrderNew","orderNew");
//        c.exchangeOrderCancel = p.get("exchangeOrderCancel","orderCancel");
//        c.topicSubscriptions = p.get("topicSubscriptions","subscriptions");
//        c.topicPositionSnapshot = p.get("topicPositionSnapshot","topicPositionSnapshot");
//        c.listeningForReceipts = p.getAsBoolean("listeningForReceipts", false);
//        c.storePositionsToDisk = p.getAsBoolean("storePositionsToDisk", false);
//        c.positionFilename = p.get("positionFilename", "PositionDefault.csv");
        return c;
    }

    private static Config createRealConfig() {
        String configString = getInitRealDefaultString();
        Config config = Config.fromProperties(PropertiesReader.createFromConfigString(configString));
        return config;
    }

    public static IProvideProperties createRealProperties() {
        String configString = getInitRealDefaultString();
        return PropertiesReader.createFromConfigString(configString);
    }

    private static String getInitRealDefaultString() {
        String CR = FileTool.getLineSeparator();
        String configString =
        "serverIP = 127.0.0.1" + CR +
        "exchangeMarketData = marketdata" + CR +
        "exchangeReceipts = receipts" + CR +
        "exchangeSubscription = subscriptions" + CR +
        "exchangePositionRequest = positionrequests" + CR +
        "exchangePositionSnapshot = positionsnapshots" + CR +
        "exchangeOrderNew = orderNew" + CR +
        "exchangeOrderCancel = orderCancel" + CR +
        "topicSubscriptions = subscriptions" + CR +
        "topicPositionSnapshot = topicPositionSnapshot" + CR +
        "listeningForReceipts = false" + CR +
        "storePositionsToDisk = false" + CR +
        "positionFilename = Position.csv" + CR;
        return configString;
    }

    private static Config createTestConfig() {
        String configString = getInitTestDefaultString();
        Config config = Config.fromProperties(PropertiesReader.createFromConfigString(configString));
        return config;
    }

    public static IProvideProperties createTestProperties() {
        String configString = getInitTestDefaultString();
        return PropertiesReader.createFromConfigString(configString);
    }


    private static String getInitTestDefaultString() {
        String CR = FileTool.getLineSeparator();
        String configString =
        "serverIP = 127.0.0.1" + CR +
        "exchangeMarketData = marketdataTest"+ CR +
        "exchangeReceipts = receiptsTest"+ CR +
        "exchangeSubscription = subscriptionsTest"+ CR +
        "exchangePositionRequest = positionrequestsTest"+ CR +
        "exchangePositionSnapshot = positionsnapshotsTest"+ CR +
        "exchangeOrderNew = orderNewTest"+ CR +
        "exchangeOrderCancel = orderCancelTest"+ CR +
        "topicSubscriptions = subscriptionsTest"+ CR +
        "topicPositionSnapshot = topicPositionSnapshotTest"+ CR +
        "listeningForReceipts = false"+ CR +
        "storePositionsToDisk = false"+ CR +
        "positionFilename = PositionTest.csv";
        return configString;
    }

    public void setProperties(IProvideProperties properties) {
        this.properties = properties;
    }

    public Config() {
    }

    private IProvideProperties properties;

//    private String serverIP;
//    private String exchangeMarketData;
//    private String exchangeReceipts;
//    private String exchangeSubscription;
//    private String exchangePositionRequest;
//    private String exchangePositionSnapshot;
//    private String exchangeOrderNew;
//    private String exchangeOrderCancel;
//    private String topicSubscriptions;
//    private String topicPositionSnapshot;
//    private boolean listeningForReceipts;
//    private boolean storePositionsToDisk;
//    private String positionFilename;


}
