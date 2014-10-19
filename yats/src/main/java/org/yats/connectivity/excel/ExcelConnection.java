package org.yats.connectivity.excel;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelConnection implements
        IConsumeBulkPriceData, IConsumeReceipt, IConsumeReports, IConsumePositionSnapshot, IConsumeAxisChanges,
        ISendBulkSettings {

    final Logger log = LoggerFactory.getLogger(ExcelConnection.class);

    // for prices
    @Override
    public void onFirstRowChange(Collection<String> firstRow) {
    }

    // for prices
    @Override
    public void onFirstColumnChange(Collection<String> firstColumn) {
        int oldKnownProductsSize = knownProducts.size();
        updateKnownProducts(firstColumn);
        int newProducts = knownProducts.size() - oldKnownProductsSize;
        if(newProducts>0) log.info("newProducts: "+newProducts);
    }

    @Override
    public void sendBulkSettings(Collection<IProvideProperties> all) {
        for(IProvideProperties p : all) strategyToBusConnection.sendSettings(p);
    }

    private void updateKnownProducts(Collection<String> firstColumn) {
        for(String pid : firstColumn) {
            if(!knownProducts.containsKey(pid)) {
                if(productList.containsProductWith(pid)) {
                    subscribe(pid);
                    knownProducts.put(pid,pid);
                }
            }
        }
    }

    @Override
    public void onBulkPriceData(Collection<? extends PriceData> dataList) {
        DateTime startCopy = DateTime.now();
        List<MatrixItem> all = new ArrayList<MatrixItem>();
        for(PriceData data : dataList) {
            String pid = data.getProductId();
            MatrixItem timestamp = new MatrixItem(pid, "timestamp", data.getTimestamp().toString());
            all.add(timestamp);
            all.addAll(getBookSideAsMatrixItems(data, BookSide.BID));
            all.addAll(getBookSideAsMatrixItems(data, BookSide.ASK));
        }
        Duration dCopy = new Duration(startCopy, DateTime.now());
//        log.info("bulkCopy: " + dCopy.getMillis());

//        DateTime startSheet = DateTime.now();
        sheetAccessPrices.updateMatrix(all);
//        Duration d = new Duration(startSheet, DateTime.now());
//        log.info("sheetAccessPrices: " + d.getMillis());

    }

    @Override
    public void onReceipt(Receipt receipt) {

    }

    @Override
    public void onPositionSnapshot(PositionSnapshot snapshot) {
        List<MatrixItem> allSnapshotItems = new ArrayList<MatrixItem>();
        for (AccountPosition p : snapshot.getAllPositions()) {
            allSnapshotItems.add(new MatrixItem(p.getProductId(), p.getInternalAccount(), p.getSize().toString()));
        }
        sheetAccessPositions.updateMatrix(allSnapshotItems);
    }

    @Override
    public void onReport(IProvideProperties p, boolean hasMoreReports) {
        if(p.exists("sendAllSettings")) {
            sheetAccessSettings.initiateResendOfSettings();
            return;
        }
        if (!p.exists(STRATEGYNAME)) {
            log.error("strategy report without strategyName found: " + p.toString());
            return;
        }
        populateReportsMap(p);
        if (hasMoreReports) return;
        sheetAccessReports.updateMatrix(reportsMap.values());
        reportsMap.clear();
    }

    private void populateReportsMap(IProvideProperties p) {
        String strategyName = p.get(STRATEGYNAME);
        for (String key : p.getKeySet()) {
            if (key.compareTo(STRATEGYNAME) == 0) continue;
            String value = p.get(key);
            MatrixItem m = new MatrixItem(strategyName, key, value);
            reportsMap.put(m.getKey(), m);
        }
    }

    public void subscribe(String pid) {
        strategyToBusConnection.subscribeBulk(pid, this);
    }

    public void startDDE() {
        try {
            System.out.println("connecting to Excel sheets...");
            sheetAccessPositions.init("Excel", prop.get("DDEPathToExcelFileWPositions"));
            sheetAccessReports.init("Excel", prop.get("DDEPathToExcelFileWReports"));
            sheetAccessPrices.init("Excel", prop.get("DDEPathToExcelFileWPrices"));
            sheetAccessSettings.init("Excel", prop.get("DDEPathToExcelFileWSettings"));
            System.out.println("done.");
            Collection<String> pidList = sheetAccessPrices.getRowIdList();
            subscribeAllProductIds(pidList);
        } catch (DDELink.ConversationException e) {
            System.out.println("DDEException: " + e.getMessage());
            close();
            System.exit(-1);
        }
        strategyToBusConnection.setBulkPriceDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        strategyToBusConnection.setReportsConsumer(this);
        strategyToBusConnection.setPositionSnapshotConsumer(this);
//        sheetAccessSettings.start();
    }


    public void stopDDE() {
        try {
            Tool.sleepFor(500);
            sheetAccessPositions.disconnect();
            sheetAccessReports.disconnect();
            sheetAccessPrices.disconnect();
        } catch (DDELink.ConversationException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        strategyToBusConnection.close();
    }

    public void go() throws InterruptedException, IOException {
        log.info("Starting ExcelConnection...");

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

    private void subscribeAllProductIds(Collection<String> pidList) {
        for (String pid : pidList) {
            if(productList.containsProductWith(pid))
                subscribe(pid);
        }
    }

    public ExcelConnection(IProvideProperties _prop,
                           IProvideProduct _products,
                           IProvideDDEConversation _priceConversation,
                           IProvideDDEConversation _reportConversation,
                           IProvideDDEConversation _positionConversation,
                           IProvideDDEConversation _settingsConversation


    ) {
        prop = _prop;
        productList=_products;
        if (!Tool.isWindows()) System.out.println("This is not Windows! DDELink will not work!");
        reportsMap = new ConcurrentHashMap<String, MatrixItem>();
        sheetAccessPositions = new SheetAccess(_positionConversation);
        sheetAccessReports = new SheetAccess(_reportConversation);
        sheetAccessReports.setSnapShotMode(false);
        sheetAccessReports.setNaString("");
        sheetAccessPrices = new SheetAccess(_priceConversation);
        sheetAccessPrices.setNaString("");
        sheetAccessPrices.setFirstRowListener(this);
        knownProducts = new ConcurrentHashMap<String, String>();
        sheetAccessSettings =new SheetAccess(_settingsConversation);
        sheetAccessSettings.setSettingsSender(this);

        strategyToBusConnection = new StrategyToBusConnection(_prop);

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Collection<? extends MatrixItem> getBookSideAsMatrixItems(PriceData data, BookSide side) {
        OfferBook book = data.getBook();
        List<MatrixItem> all = new ArrayList<MatrixItem>();
        for(int i=0; i<book.getDepth(side); i++) {
            BookRow r = book.getBookRow(side,i);
            MatrixItem size = new MatrixItem(data.getProductId(), "size"+side.toString()+i, r.getSize().toString());
            all.add(size);
            MatrixItem price = new MatrixItem(data.getProductId(), "price"+side.toString()+i, r.getPrice().toString());
            all.add(price);
        }
        return all;
    }


    private static String STRATEGYNAME = "strategyName";

    private SheetAccess sheetAccessSettings;
    private SheetAccess sheetAccessPositions;
    private SheetAccess sheetAccessReports;
    private SheetAccess sheetAccessPrices;
    private StrategyToBusConnection strategyToBusConnection;
    private IProvideProperties prop;
    private IProvideProduct productList;
    private ConcurrentHashMap<String, MatrixItem> reportsMap;
    private ConcurrentHashMap<String, String> knownProducts;

} // class

