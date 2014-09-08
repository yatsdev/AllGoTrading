package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelConnection implements DDEClientEventListener,
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
                    conversation.poke("R" + j + "C2:R" + j + "C42", marketDataString);


                } catch (DDEException e) {
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


        try {

            String StrategyNameOfThisReport = null;
            ConcurrentHashMap<String, String> hashmapKeyValue = new ConcurrentHashMap();

            if (!p.exists("strategyName")) {
                log.error("strategy without name found:" + p.toString());
                return;
            }
            if (p.size() < 2) {
                log.error("strategy without key/values found:" + p.toString());
                return;
            } else {


                for (String key : p.getKeySet()) {

                    if (!(key.compareTo("strategyName") == 0)) {
                        String value = p.get(key);
                        hashmapKeyValue.put(key, value);

                    } else {

                        StrategyNameOfThisReport = p.get("strategyName");
                    }
                }

                theMatrix.put(StrategyNameOfThisReport, hashmapKeyValue);

            }


            addKeyValuesNotPresentInR1(StrategyNameOfThisReport);
            addStrategyNamesNotPresentInC1(StrategyNameOfThisReport);

            //Finally poking data from reports in a per row poke transaction fashion

            int strategyIndex;
            for (int q = 0; q < StrategyNames.size(); q++) {
                strategyIndex = q + 2;
                if (StrategyNames.elementAt(q).compareTo(p.get("strategyName")) == 0) {
                    conversationReports.poke("R" + strategyIndex + "C2:R" + strategyIndex + "C" + positionKeyValues, generatePerRowPokeString(KeyValues, hashmapKeyValue));//RightMostCell
                }
            }

        } catch (DDEException e) {
            e.printStackTrace();
        }

        System.out.println("Strategy reports: " + PropertiesReader.toString(p));

        //lets send the report back as settings to test the way back to the strategy
//        strategyToBusConnection.sendSettings(p);
    }

    private synchronized void addStrategyNamesNotPresentInC1(String StrategyNameOfThisReport) {
        if (!StrategyNames.contains(StrategyNameOfThisReport)) {
            try {

                conversationReports.poke("R" + positionStrategyName + "C1", StrategyNameOfThisReport);

            } catch (DDEException e) {
                e.printStackTrace();
            }
            StrategyNames.add(StrategyNameOfThisReport);
            positionStrategyName = StrategyNames.size() + 2;
        }
    }


    private synchronized void addKeyValuesNotPresentInR1(String nameOfThisStrategy) {
        Enumeration<String> keys = theMatrix.get(nameOfThisStrategy).keys();
        while (keys.hasMoreElements()) {
            String currentKey = keys.nextElement();
            if (!KeyValues.contains(currentKey)) {
                try {

                    conversationReports.poke("R1C" + positionKeyValues, currentKey);

                } catch (DDEException e) {
                    e.printStackTrace();
                }
                KeyValues.add(currentKey);
                positionKeyValues = KeyValues.size() + 2;
            }
        }
    }

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    public void subscribe(String pid) {
        strategyToBusConnection.subscribe(pid, this);
    }

    public boolean isExcelRunning() {
        return false;
    }

    public boolean isWorkbookOpenInExcel() {
        return false;
    }

    public void startExcelWithWorkbook() {
    }

    public void startDDE() {
        try {
            System.out.print("conversation.connect...");
            conversation.setTimeout(3000);
            conversation.connect("Excel", prop.get("DDEPathToExcelFile"));
            initConversationPositions();
            System.out.println("done.");
            System.out.print("conversation.request...");
            String s = conversation.request("C1");
            System.out.println("done.");
            parseProductIds(s);
            subscribeAllProductIds();
            System.out.print("conversation.startAdvice...");
            conversation.startAdvice("C1");
            System.out.println("done.");
        } catch (DDEMLException e) {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode()) + " " + e.getMessage());
            close();
            System.exit(-1);
        } catch (DDEException e) {
            System.out.println("DDEException: " + e.getMessage());
            close();
            System.exit(-1);
        }
        strategyToBusConnection.setPriceDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        strategyToBusConnection.setReportsConsumer(this);
        strategyToBusConnection.setPositionSnapshotConsumer(this);

    }

    public void startDDEReports() {
        try {
            System.out.print("conversationReports.connect...");
            conversationReports.setTimeout(3000);
            conversationReports.connect("Excel", prop.get("DDEPathToExcelFileWReports"));
            System.out.println("done.");
            System.out.print("conversationReports.request...");
            String data = conversationReports.request("C1");
            String data2 = conversationReports.request("R1");
            System.out.println("conversationReports done.");
            parseStrategyNames(data);
            parsekeyValues(data2);
            System.out.print("conversationReports.startAdvice...");
            System.out.println("done.");
        } catch (DDEMLException e) {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode())
                    + " " + e.getMessage());
            close();
            System.exit(-1);
        } catch (DDEException e) {
            System.out.println("DDEException: " + e.getMessage());
            close();
            System.exit(-1);
        }
    }

    public void stopDDE() {
        try {
            shutdown = true;
            Tool.sleepFor(500);
            conversation.stopAdvice("C1");//This means all elements in column 1
            conversation.disconnect();
            conversationPositions.disconnect();
        } catch (DDEException e) {
            e.printStackTrace();
        }
    }

    public void stopDDEReports() {
        try {
            shutdown = true;
            Tool.sleepFor(500);
            conversationReports.disconnect();
        } catch (DDEException e) {
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
            startDDEReports();
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
            stopDDEReports();
        }
        if (Tool.isWindows()) Thread.sleep(1000);
        if (!Tool.isWindows()) Thread.sleep(60000);

        log.info("ExcelConnection done.");
        System.exit(0);
    }

    public ExcelConnection(IProvideProperties _prop) {
        shutdown = false;
        prop = _prop;
        positionProducts = new ArrayList<String>();
        positionProductsMap = new ConcurrentHashMap<String, String>();
        positionAccounts = new ArrayList<String>();
        positionAccountsMap = new ConcurrentHashMap<String, String>();
        productAccount2PositionMap = new ConcurrentHashMap<String, AccountPosition>();
        if (Tool.isWindows()) {
            try {
                conversation = new DDEClientConversation();  // cant use this on Linux
                conversation.setEventListener(this);
                conversationReports = new DDEClientConversation();
                conversationReports.setEventListener(this);
                conversationPositions = new DDEClientConversation();
                conversationPositions.setEventListener(new PositionConversationListener(this));
            } catch (UnsatisfiedLinkError e) {
                log.error(e.getMessage());
                close();
                System.exit(-1);
            }
        } else {
            System.out.println("This is not Windows! DDEClient will not work!");
//            System.exit(0);
        }
        strategyToBusConnection = new StrategyToBusConnection(_prop);
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

    private int positionStrategyName;
    private int positionKeyValues;
    private ConcurrentHashMap<String, ConcurrentHashMap> theMatrix = new ConcurrentHashMap();
    private Vector<String> currentProductIDs = new Vector<String>();
    private Vector<String> StrategyNames = new Vector<String>();
    private Vector<String> KeyValues = new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;
    private DDEClientConversation conversationReports;
    private IProvideProperties prop;
    private boolean shutdown;


    //////////////////////////////////////////////////////// POSITION SNAPSHOTS

    private synchronized void onPositionLayoutChanged(String sheetId, String observedCellId, String data) {
        if (observedCellId.compareTo("C1") == 0) parsePositionProducts(data);
        if (observedCellId.compareTo("R1") == 0) parsePositionAccounts(data);
    }

    @Override
    public synchronized void onPositionSnapshot(PositionSnapshot snapshot) {
        updatePositionsAxis(snapshot);
        productAccount2PositionMap.clear();
        ConcurrentHashMap<String, String> productsToUpdate = getProductsToUpdate(snapshot);
        updateProductAccount2PositionMap(snapshot);

        for (String p : productsToUpdate.keySet()) {
            pokeRowForAllAccountsOfProduct(p);
        }
    }

    private void pokeRowForAllAccountsOfProduct(String p) {
        String s = "";
        int index = positionProducts.indexOf(p);
        if (index < 0) return;
        int row = 2 + index;
        int count = 1;
        for (String account : positionAccounts) {
            count++;
            String key = AccountPosition.getKey(p, account);
            if (productAccount2PositionMap.containsKey(key)) {
                AccountPosition ap = productAccount2PositionMap.get(key);
                s += ap.getSize().toString();
            } else s += "n/a";
            s += "\t";
        }
        pokePositionsRow(row, count, s);
    }

    private void updateProductAccount2PositionMap(PositionSnapshot snapshot) {
        for (AccountPosition pos : snapshot.getAllPositions()) {
            String key = pos.getKey();
            if (productAccount2PositionMap.containsKey(key)) {
                AccountPosition old = productAccount2PositionMap.get(key);
                if (pos.isSameAs(old)) continue;
            }
            productAccount2PositionMap.put(key, pos);
        }
    }

    private ConcurrentHashMap<String, String> getProductsToUpdate(PositionSnapshot snapshot) {
        ConcurrentHashMap<String, String> productsToUpdate = new ConcurrentHashMap<String, String>();
        for (AccountPosition pos : snapshot.getAllPositions()) {
            String key = pos.getKey();
            if (productAccount2PositionMap.containsKey(key)) {
                AccountPosition old = productAccount2PositionMap.get(key);
                if (pos.isSameAs(old)) continue;
            }
            productsToUpdate.put(pos.getProductId().toString(), pos.getProductId().toString());
        }
        return productsToUpdate;
    }

    private void updatePositionsAxis(PositionSnapshot snapshot) {
        int originalPositionProductsSize = positionProducts.size();

        for (AccountPosition p : snapshot.getAllPositions()) {
            updateList(positionProducts, positionProductsMap, p.getProductId());
        }
        int originalPositionAccountsSize = positionAccounts.size();
        for (AccountPosition p : snapshot.getAllPositions()) {
            updateList(positionAccounts, positionAccountsMap, p.getInternalAccount());
        }

        boolean updateProductsColumn = positionProducts.size() > originalPositionProductsSize;
        if (updateProductsColumn) pokePositionsProducts();
        boolean updateAccountsColumn = positionAccounts.size() > originalPositionAccountsSize;
        if (updateAccountsColumn) pokePositionsAccounts();
    }

    private void updateList(List<String> list, ConcurrentHashMap<String, String> map, String item) {
        if (map.containsKey(item)) return;
        list.add(item);
        map.put(item, item);
    }

    private void pokePositionsProducts() {
        String data = "\r\n";
        for (String s : positionProducts) {
            data += s + "\r\n";
        }
        pokePositions("C1", data);
    }

    private void pokePositionsAccounts() {
        String data = "\t";
        for (String s : positionAccounts) {
            data += s + "\t";
        }
        pokePositions("R1", data);
    }

    private void pokePositionsRow(int row, int count, String what) {
        if (row == 1) {
            System.out.println("Row 1 should never be poked to!");
            System.exit(-1);
        }
        pokePositions("R" + row + "C2:R" + row + "C" + count, what);
    }

    private void pokePositions(String where, String what) {
        while (true) {
            try {
                conversationPositions.poke(where, what);
                return;
            } catch (DDEException e) {
//            e.printStackTrace();
                System.out.println("Excel seems busy. waiting...");
                Tool.sleepFor(3000);
            }
        }
    }

    private void parsePositionProducts(String data) {
        String productIdsString = data.replace("\r\n", "\t");
        String[] parts = productIdsString.split("\t");
        positionProducts.clear();
        positionProductsMap.clear();
        positionProducts.addAll(Arrays.asList(parts));
        for (String s : positionProducts) positionProductsMap.put(s, s);
        if (positionProducts.size() > 0) positionProducts.remove(0);
    }

    private void parsePositionAccounts(String data) {
        String accountsString = data.replace("\r\n", "\t");
        String[] parts = accountsString.split("\t");
        positionAccounts.clear();
        positionAccountsMap.clear();
        positionAccounts.addAll(Arrays.asList(parts));
        for (String s : positionAccounts) positionAccountsMap.put(s, s);
        if (positionAccounts.size() > 0) positionAccounts.remove(0);
    }

    private void initConversationPositions() throws DDEException {
        conversationPositions.setTimeout(2000);
        conversationPositions.connect("Excel", prop.get("DDEPathToExcelFileWPositions"));
        String positionProductsString = conversationPositions.request("C1");
        parsePositionProducts(positionProductsString);
        String positionAccountsString = conversationPositions.request("R1");
        parsePositionAccounts(positionAccountsString);
        conversationPositions.startAdvice("C1");
        conversationPositions.startAdvice("R1");
    }

    private ArrayList<String> positionProducts;
    private ConcurrentHashMap<String, String> positionProductsMap;
    private ArrayList<String> positionAccounts;
    private ConcurrentHashMap<String, String> positionAccountsMap;
    private DDEClientConversation conversationPositions;
    private ConcurrentHashMap<String, AccountPosition> productAccount2PositionMap;


    private static class PositionConversationListener implements DDEClientEventListener {
        @Override
        public void onDisconnect() {
        }

        @Override
        public void onItemChanged(String s, String s2, String s3) {
            excelConnection.onPositionLayoutChanged(s, s2, s3);
        }

        private PositionConversationListener(ExcelConnection _xlConn) {
            excelConnection = _xlConn;
        }

        private ExcelConnection excelConnection;
    }


} // class

