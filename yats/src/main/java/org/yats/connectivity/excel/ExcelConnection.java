package org.yats.connectivity.excel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelConnection implements
        IConsumePriceData, IConsumeReceipt, IConsumeReports, IConsumePositionSnapshot {

    final Logger log = LoggerFactory.getLogger(ExcelConnection.class);

    @Override
    public void onPriceData(PriceData marketData) {

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
        excelToolsPositions.updateMatrix(allSnapshotItems);
    }

    @Override
    public void onReport(IProvideProperties p, boolean hasMoreReports) {
        if (!p.exists(STRATEGYNAME)) {
            log.error("strategy report without strategyName found: " + p.toString());
            return;
        }
        populateReportsMap(p);
        if (hasMoreReports) return;
        excelToolsReports.updateMatrix(reportsMap.values());
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

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    public void subscribe(String pid) {
        strategyToBusConnection.subscribe(pid, this);
    }

    // todo: need callback whenever user changes first row for prices
    public void startDDE() {
        try {
            System.out.print("conversation.connect...");
            excelToolsPositions.init("Excel", prop.get("DDEPathToExcelFileWPositions"));
            excelToolsReports.init("Excel", prop.get("DDEPathToExcelFileWReports"));
            excelToolsPrices.init("Excel", prop.get("DDEPathToExcelFileWPrices"));
            System.out.println("done.");
            System.out.print("conversation.request...");
            Collection<String> pidList = excelToolsPrices.getRowIdList();
            subscribeAllProductIds(pidList);
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
            excelToolsPositions.disconnect();
            excelToolsReports.disconnect();
            excelToolsPrices.disconnect();
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

    private void subscribeAllProductIds(Collection<String> pidList) {
        for (String pid : pidList) {
            if(productList.containsProductWith(pid))
                subscribe(pid);
        }
    }

    private void parseProductIds(String data) {
        String[] parts = data.split("\r\n");
        currentProductIDs = new Vector<String>(Arrays.asList(parts));
    }




    public ExcelConnection(IProvideProperties _prop,
                           IProvideProduct _products,
                           IProvideDDEConversation _priceConversation,
                           IProvideDDEConversation _reportConversation,
                           IProvideDDEConversation _positionConversation


    ) {
        shutdown = false;
        prop = _prop;
        productList=_products;
        if (!Tool.isWindows()) System.out.println("This is not Windows! DDELink will not work!");
        reportsMap = new ConcurrentHashMap<String, MatrixItem>();
        pricesMap = new ConcurrentHashMap<String, MatrixItem>();
        excelToolsPositions = new ExcelTools(_positionConversation);
        excelToolsReports = new ExcelTools(_reportConversation);
        excelToolsReports.setSnapShotMode(false);
        excelToolsReports.setNaString("");
        excelToolsPrices = new ExcelTools(_priceConversation);
        excelToolsPrices.setNaString("");

        strategyToBusConnection = new StrategyToBusConnection(_prop);

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static String STRATEGYNAME = "strategyName";

    private ExcelTools excelToolsPositions;
    private ExcelTools excelToolsReports;
    private ExcelTools excelToolsPrices;

    private Vector<String> currentProductIDs = new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;

    private IProvideProperties prop;
    private IProvideProduct productList;
    private boolean shutdown;

    private ConcurrentHashMap<String, MatrixItem> reportsMap;
    private ConcurrentHashMap<String, MatrixItem> pricesMap;


} // class

