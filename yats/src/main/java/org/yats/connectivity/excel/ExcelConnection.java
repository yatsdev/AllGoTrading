package org.yats.connectivity.excel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelConnection implements DDELinkEventListener,
        IConsumePriceData, IConsumeReceipt, IConsumeReports, IConsumePositionSnapshot {

    final Logger log = LoggerFactory.getLogger(ExcelConnection.class);

    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect()");
    }

    @Override
    public void onItemChanged(String topic, String item, String data) {
        if (topic.compareTo(prop.get("DDEPathToExcelFile")) == 0) {
            parseProductIds(data);
            subscribeAllProductIds();
        }

        if (topic.compareTo(prop.get("DDEPathToExcelFileWReports")) == 0) {
            if (data.startsWith("\t")) {
                parseStrategyNames(data);
            } else if (data.startsWith("\r")) {
                parsekeyValues(data);
            }
        }
    }

    @Override
    public void onPriceData(PriceData marketData) {
        for (int i = 1; i < currentProductIDs.size(); i++) {
            int j = i + 1;
            if (marketData.hasProductId(currentProductIDs.elementAt(i))) {
                try {

                    String marketDataString = new String(marketData.getTimestamp().toString() + "\t"
                            + marketData.getBidSize().toString() + "\t"
                            + marketData.getBid().toString() + "\t"
                            + marketData.getAskSize().toString()
                            + "\t" + marketData.getAsk().toString());


                    for (int n = 1; n < 10; n++) {

                        int p = n + 1;

                        String lvnBidSize = new String("");
                        String lvnBidPrice = new String("");
                        String lvnAskSize = new String("");
                        String lvnAskPrice = new String("");

                        if (marketData.getBook().getDepth(BookSide.BID) >= p) {
                            lvnBidSize = marketData.getBook().getBookRow(BookSide.BID, n).getSize().toString();
                            lvnBidPrice = marketData.getBook().getBookRow(BookSide.BID, n).getPrice().toString();
                        }

                        if (marketData.getBook().getDepth(BookSide.ASK) >= p) {
                            lvnAskSize = marketData.getBook().getBookRow(BookSide.ASK, n).getSize().toString();
                            lvnAskPrice = marketData.getBook().getBookRow(BookSide.ASK, n).getPrice().toString();
                        }

                        marketDataString = marketDataString + "\t" + lvnBidSize + "\t" + lvnBidPrice + "\t" + lvnAskSize + "\t" + lvnAskPrice;


                    }

//                    log.info(marketDataString);
                    if (shutdown) return;
                    conversationPriceData.poke("R" + j + "C2:R" + j + "C42", marketDataString);


                } catch (DDELink.ConversationException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }

        // use strategyToBusConnection.sendSettings(...) to send settings to the strategies like in onReport(..) below

    }

    @Override
    public void onReceipt(Receipt receipt) {

    }


    @Override
    public synchronized void onReport(IProvideProperties p,boolean hasMoreReports) {


//        try {
//
//            String StrategyNameOfThisReport = null;
//            ConcurrentHashMap<String, String> hashmapKeyValue = new ConcurrentHashMap();
//
//            if (!p.exists("strategyName")) {
//                log.error("strategy without name found:" + p.toString());
//                return;
//            }
//            if (p.size() < 2) {
//                log.error("strategy without key/values found:" + p.toString());
//                return;
//            } else {
//
//
//                for (String key : p.getKeySet()) {
//
//                    if (!(key.compareTo("strategyName") == 0)) {
//                        String value = p.get(key);
//                        hashmapKeyValue.put(key, value);
//
//                    } else {
//
//                        StrategyNameOfThisReport = p.get("strategyName");
//                    }
//                }
//
//                theMatrix.put(StrategyNameOfThisReport, hashmapKeyValue);
//
//            }
//
//
//            addKeyValuesNotPresentInR1(StrategyNameOfThisReport);
//            addStrategyNamesNotPresentInC1(StrategyNameOfThisReport);
//
//            //Finally poking data from reports in a per row poke transaction fashion
//
//            int strategyIndex;
//            for (int q = 0; q < StrategyNames.size(); q++) {
//                strategyIndex = q + 2;
//                if (StrategyNames.elementAt(q).compareTo(p.get("strategyName")) == 0) {
//                    excelToolsReports.poke("R" + strategyIndex + "C2:R" + strategyIndex + "C" + positionKeyValues, generatePerRowPokeString(KeyValues, hashmapKeyValue));//RightMostCell
//                }
//            }
//
//        } catch (DDELink.ConversationException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Strategy reports: " + PropertiesReader.toString(p));
//
//        //lets send the report back as settings to test the way back to the strategy
////        strategyToBusConnection.sendSettings(p);
    }

//    private synchronized void addStrategyNamesNotPresentInC1(String StrategyNameOfThisReport) {
//        if (!StrategyNames.contains(StrategyNameOfThisReport)) {
//            try {
//
//                excelToolsReports.poke("R" + positionStrategyName + "C1", StrategyNameOfThisReport);
//
//            } catch (DDELink.ConversationException e) {
//                e.printStackTrace();
//            }
//            StrategyNames.add(StrategyNameOfThisReport);
//            positionStrategyName = StrategyNames.size() + 2;
//        }
//    }


//    private synchronized void addKeyValuesNotPresentInR1(String nameOfThisStrategy) {
//        Enumeration<String> keys = theMatrix.get(nameOfThisStrategy).keys();
//        while (keys.hasMoreElements()) {
//            String currentKey = keys.nextElement();
//            if (!KeyValues.contains(currentKey)) {
//                try {
//
//                    excelToolsReports.poke("R1C" + positionKeyValues, currentKey);
//
//                } catch (DDELink.ConversationException e) {
//                    e.printStackTrace();
//                }
//                KeyValues.add(currentKey);
//                positionKeyValues = KeyValues.size() + 2;
//            }
//        }
//    }

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    public void subscribe(String pid) {
        strategyToBusConnection.subscribe(pid, this);
    }

    public void startDDE() {
        try {
            System.out.print("conversation.connect...");
            conversationPriceData.setTimeout(3000);
            conversationPriceData.connect("Excel", prop.get("DDEPathToExcelFile"));
            excelToolsPositions.init("Excel", prop.get("DDEPathToExcelFileWPositions"));
            excelToolsPositions.init("Excel", prop.get("DDEPathToExcelFileWReports"));
            System.out.println("done.");
            System.out.print("conversation.request...");
            String s = conversationPriceData.request("C1");
            System.out.println("done.");
            parseProductIds(s);
            subscribeAllProductIds();
            System.out.print("conversation.startAdvice...");
            conversationPriceData.startAdvice("C1");
            System.out.println("done.");
        } catch (DDELink.ConversationException e) {
            System.out.println("DDEException: " + e.getMessage());
            close();
            System.exit(-1);
        }
        strategyToBusConnection.setPriceDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        strategyToBusConnection.setReportsConsumer(this);
        strategyToBusConnection.setPositionSnapshotConsumer(this);

    }

    public void stopDDE() {
        try {
            shutdown = true;
            Tool.sleepFor(500);
            conversationPriceData.stopAdvice("C1");//This means all elements in column 1
            conversationPriceData.disconnect();
            excelToolsPositions.disconnect();
            excelToolsReports.disconnect();
        } catch (DDELink.ConversationException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        strategyToBusConnection.close();
    }

    public void go() throws InterruptedException, IOException {
        log.info("Starting ExcelConnection...");

//        thread = new Thread(this);
//        thread.start();

        if (Tool.isWindows()) {
            startDDE();
        }
        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        close();
        if (Tool.isWindows()) {
            stopDDE();
        }
        if (Tool.isWindows()) Thread.sleep(1000);
        if (!Tool.isWindows()) Thread.sleep(60000);

        log.info("ExcelConnection done.");
        System.exit(0);
    }


    ///////////////////////////////////////////////////////////////////////////////////

    private void subscribeAllProductIds() {
        for (String pid : currentProductIDs) {
            subscribe(pid);
        }
    }

    private void parseProductIds(String data) {
        String[] parts = data.split("\r\n");
        currentProductIDs = new Vector<String>(Arrays.asList(parts));
    }

    private void parseStrategyNames(String strategyNames) {

        String[] parts = strategyNames.split("\r\n");
        StrategyNames = new Vector<String>(Arrays.asList(parts));
        if(StrategyNames.size()>0){StrategyNames.removeElementAt(0);}//R1C1 is empty
        positionStrategyName = StrategyNames.size() + 2;
    }

    private void parsekeyValues(String keyValues) {
        String[] parts = keyValues.split("\t");
        KeyValues = new Vector<String>(Arrays.asList(parts));
        if(KeyValues.size()>0&&!(KeyValues.elementAt(0).compareTo("\r\n")==0)) {
            KeyValues.removeElementAt(0);}//R1C1 is empty
            String lastElement = KeyValues.lastElement();
            String lastElement2 = lastElement.replace("\r\n", "");
            KeyValues.removeElementAt(KeyValues.size() - 1);
            KeyValues.add(lastElement2);

            if (KeyValues.lastElement().compareTo("") == 0) {
                KeyValues.remove("");
            }

        positionKeyValues = KeyValues.size() + 2;
    }


    private synchronized String generatePerRowPokeString(Vector<String> KeyValues, ConcurrentHashMap<String, String> hashmapKeyValue) {

        String PerRowPokeString = new String();

        for (int i = 0; i < KeyValues.size(); i++) {//R1C1 is empty
            if (i == 0) {
                if (hashmapKeyValue.containsKey(KeyValues.elementAt(i))) {
                    PerRowPokeString = hashmapKeyValue.get(KeyValues.elementAt(i));
                }

            } else {
                if (hashmapKeyValue.containsKey(KeyValues.elementAt(i))) {
                    PerRowPokeString = PerRowPokeString + "\t" + hashmapKeyValue.get(KeyValues.elementAt(i));

                } else {
                    PerRowPokeString = PerRowPokeString + "\t";  //For blank cells

                }

            }
        }
        return PerRowPokeString;
    }



    @Override
    public synchronized void onPositionSnapshot(PositionSnapshot snapshot) {
        List<MatrixItem> allSnapshotItems = new ArrayList<MatrixItem>();
        for (AccountPosition p : snapshot.getAllPositions()) {
            allSnapshotItems.add(new MatrixItem(p.getProductId(), p.getInternalAccount(), p.getSize().toString()));
        }
        excelToolsPositions.updateMatrix(allSnapshotItems);
    }


    public ExcelConnection(IProvideProperties _prop,
                           IProvideDDEConversation _priceConversation,
                           IProvideDDEConversation _reportConversation,
                           IProvideDDEConversation _positionConversation


    ) {
        shutdown = false;
        prop = _prop;
        if (!Tool.isWindows()) {
            System.out.println("This is not Windows! DDELink will not work!");
        }
        conversationPriceData = _priceConversation;
        conversationPriceData.setEventListener(this);
        excelToolsPositions = new ExcelTools(_positionConversation);
        excelToolsReports = new ExcelTools(_reportConversation);

        strategyToBusConnection = new StrategyToBusConnection(_prop);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    private ExcelTools excelToolsPositions;
    private ExcelTools excelToolsReports;

    private int positionStrategyName;
    private int positionKeyValues;
    private ConcurrentHashMap<String, ConcurrentHashMap> theMatrix = new ConcurrentHashMap();
    private Vector<String> currentProductIDs = new Vector<String>();
    private Vector<String> StrategyNames = new Vector<String>();
    private Vector<String> KeyValues = new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private IProvideDDEConversation conversationPriceData;

    private IProvideProperties prop;
    private boolean shutdown;



} // class

