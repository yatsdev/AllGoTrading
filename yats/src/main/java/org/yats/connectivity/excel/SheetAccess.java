package org.yats.connectivity.excel;


import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.trader.StrategyBase;
import org.yats.trading.ISendBulkSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SheetAccess implements DDELinkEventListener, Runnable {

    final Logger log = LoggerFactory.getLogger(SheetAccess.class);

    @Override
    public void onDisconnect() {
    }

    static int changedCounter=0;
    @Override
    public synchronized void onItemChanged(String sheetId, String cellId, String data) {
        System.out.println("onItemChanged:"+changedCounter++);
        if (cellId.compareTo(firstColumnExcelArray) == 0) {
            String firstColumnString = ddeLink.request(firstColumnExcelArray);
            parseFirstColumn(firstColumnString);
//            updateFirstColumn(data);
        } else if (cellId.compareTo(firstRowExcelArray) == 0) {
            String firstRowString = ddeLink.request(firstRowExcelArray);
            parseFirstRow(firstRowString);
//            updateFirstRow(data);
        }
    }

    // time based sending of settings
    @Override
    public void run() {
        while(!shutdown) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.info("settingsThread shutting down.");
            }
            if(shutdown) return;
            if(resendSettings) {
                settingsRows.clear();
                resendSettings=false;
            }
            ConcurrentHashMap<String,String> oldSettingsRows = settingsRows;
            ConcurrentHashMap<String,String> change = new ConcurrentHashMap<String, String>();
            readSettingsRows();
            for(String rowString : settingsRows.values()) {
                if(oldSettingsRows.containsKey(rowString)) continue;
                change.put(rowString, rowString);
            }
            Collection<IProvideProperties> all = parseSettingsRows(change);
            while(!removedIdList.isEmpty()) {
                String removedId = removedIdList.poll();
                IProvideProperties p = new PropertiesReader();
                p.set(StrategyBase.SETTING_STRATEGYNAME, removedId);
                p.set(StrategyBase.SETTING_STRATEGYREMOVED, "true");
                all.add(p);
            }
            settingsSender.sendBulkSettings(all);

        }
    }

    public void initiateResendOfSettings() {
        resendSettings=true;
    }


    public synchronized void readSettingsRows() {
        settingsRows = new ConcurrentHashMap<String, String>();
        for(int rowIndex=0; rowIndex < rowIdList.size(); rowIndex++) {
            String rowString = readRow(rowIndex+2);
            settingsRows.put(rowString, rowString);
        }
    }
    public Collection<IProvideProperties> parseSettingsRows() {
        return parseSettingsRows(settingsRows);
    }

    public Collection<IProvideProperties> parseSettingsRows(ConcurrentHashMap<String, String> _map) {
        Collection<IProvideProperties> all = new ArrayList<IProvideProperties>();
        for(String rowString : _map.values()) {
            String tabbedRowString = rowString.replace(NL, TAB);
            String[] parts = tabbedRowString.split(TAB);
            if(parts.length<2) continue;
            IProvideProperties p = new PropertiesReader();
            String strategyName = parts[0];
            p.set("strategyName", strategyName);
            if(strategyName.length()<1) continue;
            for(int i=1; i<parts.length; i++) {
                String value = parts[i];
                if(i>columnIdList.size()) break;
                String key = columnIdList.get(i-1);
                p.set(key, value);
            }
            all.add(p);
        }
        return all;
    }

    public synchronized Collection<String> getRowIdList() {
        return rowIdList;
    }

    public void disconnect() {
        shutdown=true;
        settingsThread.interrupt();
        ddeLink.disconnect();
    }

    public synchronized void updateMatrix(Collection<MatrixItem> itemList) {
        updateAxis(itemList);
        if(snapShotMode) combiKey2ItemMap.clear();
        ConcurrentHashMap<String, String> rowIdsWithChangedData = getRowIdsToUpdate(itemList);
        updateCombiKey2ItemMap(itemList);
        updateChangedRows(rowIdsWithChangedData);
    }

    private void updateChangedRows(ConcurrentHashMap<String, String> rowIdsToUpdate) {
        DateTime startSheet = DateTime.now();
        int i=0;
        for (String p : rowIdsToUpdate.keySet()) {
            pokeRowForRowIds(p);
            i++;
        }
        Duration d = new Duration(startSheet, DateTime.now());
        log.info("updateMatrix: " + d.getMillis() + " for rows:" + i);
    }

    public void init(String applicationName, String sheetName) throws DDELink.ConversationException {
        ddeLink.setTimeout(2000);
        ddeLink.connect(applicationName, sheetName);
        String firstColumnString = ddeLink.request(firstColumnExcelArray);
        parseFirstColumn(firstColumnString);
        String firstRowString = ddeLink.request(firstRowExcelArray);
        parseFirstRow(firstRowString);
        enableFirstColumnListener();
        enableFirstRowListener();
    }

    private void enableFirstColumnListener() {
        ddeLink.startAdvice(firstColumnExcelArray);
    }

    private void disableFirstColumnListener() {
        ddeLink.stopAdvice(firstColumnExcelArray);
    }

    private void enableFirstRowListener() {
        ddeLink.startAdvice(firstRowExcelArray);
    }

    private void disableFirstRowListener() {
        ddeLink.stopAdvice(firstRowExcelArray);
    }

    public void connect(String applicationname, String sheetname) {
        ddeLink.connect(applicationname, sheetname);
    }

    public void readFirstRowFromDDE() {
        String firstRow = ddeLink.request(firstRowExcelArray);
        parseFirstRow(firstRow);
    }

    public void readFirstColumnFromDDE() {
        String firstColumn = ddeLink.request(firstColumnExcelArray);
        parseFirstColumn(firstColumn);
    }

    public int countKeysInFirstRow(){
        int count=0;
        for(String s : columnIdList) {
            count+= s.isEmpty() ? 0 : 1;
        }
        return count;
    }

    public int countKeysInFirstColumn(){
        int count=0;
        for(String s : rowIdList) {
            count+= s.isEmpty() ? 0 : 1;
        }
        return count;
    }

    public void setSnapShotMode(boolean snapShotMode) {
        this.snapShotMode = snapShotMode;
    }

    public void setNaString(String naString) {
        this.naString = naString;
    }

    public void setFirstRowListener(IConsumeAxisChanges _listenerAxisChange) {
        listenerAxisChange=_listenerAxisChange;
    }

    public void setSettingsSender(ISendBulkSettings settingsSender) {
        this.settingsSender = settingsSender;
    }

    public void start() {
        settingsThread.start();
    }

    public SheetAccess(IProvideDDEConversation _DDELink) {
        shutdown=false;
        ddeLink = _DDELink;
        ddeLink.setEventListener(this);
        removedIdList = new ConcurrentLinkedQueue<String>();
        columnIdList = new ArrayList<String>();
        mapOfColumnIds = new ConcurrentHashMap<String, String>();
        rowIdList = new ArrayList<String>();
        mapOfRowIds = new ConcurrentHashMap<String, String>();
        combiKey2ItemMap = new ConcurrentHashMap<String, MatrixItem>();
        settingsRows = new ConcurrentHashMap<String, String>();
        snapShotMode=true;
        naString="n/a";
        listenerAxisChange=new IConsumeAxisChanges() {
            @Override
            public void onFirstRowChange(Collection<String> changes) {}
            @Override
            public void onFirstColumnChange(Collection<String> changes) {}
        };
        settingsThread = new Thread(this);
        settingsSender = new ISendBulkSettings() {
            @Override
            public void sendBulkSettings(Collection<IProvideProperties> all) {}
        };
        resendSettings=false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateFirstRow(String data) {
        parseFirstRow(data);
        listenerAxisChange.onFirstRowChange(columnIdList);
    }

    private void updateFirstColumn(String data) {
        parseFirstColumn(data);
        listenerAxisChange.onFirstColumnChange(rowIdList);
    }

    private void pokeRowForRowIds(String rowId) {
        String s = getRowDataString(rowId);
        int row = getRowIndex(rowId);
        pokeRow(row, columnIdList.size() + 1, s);
    }

    private String getRowDataString(String rowId) {
        String s = "";
        for (String columnId : columnIdList) {
            String key = MatrixItem.getKey(rowId, columnId);
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem ap = combiKey2ItemMap.get(key);
                s += ap.getData();
            } else s += naString;
            s += TAB;
        }
        return s;
    }

    private int getRowIndex(String rowId) {
        int index = rowIdList.indexOf(rowId);
        if (index < 0) {
            throw new CommonExceptions.KeyNotFoundException("Can not find rowId: "+rowId);
        }
        int row = 2 + index;
        return row;
    }

    private void updateCombiKey2ItemMap(Collection<MatrixItem> list) {
        for (MatrixItem item : list) {
            String key = item.getKey();
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem oldItem = combiKey2ItemMap.get(key);
                if (item.isSameAs(oldItem)) continue;
            }
            combiKey2ItemMap.put(key, item);
        }
    }

    private ConcurrentHashMap<String, String> getRowIdsToUpdate(Collection<MatrixItem> list) {
        ConcurrentHashMap<String, String> rowIdsToUpdate = new ConcurrentHashMap<String, String>();
        for (MatrixItem item : list) {
            String key = item.getKey();
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem old = combiKey2ItemMap.get(key);
                if (item.isSameAs(old)) continue;
            }
            rowIdsToUpdate.put(item.getRowId(), item.getRowId());
        }
        return rowIdsToUpdate;
    }

    private void updateAxis(Collection<MatrixItem> itemList) {
        String firstRowStringOld = getColumnIdsString();
        for (MatrixItem p : itemList) {
            updateList(columnIdList, mapOfColumnIds, p.getColumnId());
        }
        boolean updateFirstRow = firstRowStringOld.compareTo(getColumnIdsString()) != 0;
        if (updateFirstRow) {
            pokeFirstRow();
            listenerAxisChange.onFirstRowChange(columnIdList);
        }

        String firstColumnStringOld = getRowIdsString();
        for (MatrixItem p : itemList) {
            updateList(rowIdList, mapOfRowIds, p.getRowId());
        }
        boolean updateFirstColumn = firstColumnStringOld.compareTo(getRowIdsString()) != 0;
        if (updateFirstColumn) {
            pokeFirstColumn();
            listenerAxisChange.onFirstColumnChange(rowIdList);
        }
    }

    private void updateList(List<String> list, ConcurrentHashMap<String, String> map, String item) {
        if (map.containsKey(item)) return;
        list.add(item);
        map.put(item, item);
    }

    private void pokeFirstColumn() {
        String where = "R2C1:R"+(rowIdList.size()+1)+"C1";
        String what = getRowIdsString();
        disableFirstColumnListener();
        poke(where, what);
        enableFirstColumnListener();
    }

    private String getRowIdsString() {
        String data = "";
        for (String s : rowIdList) {
            data += s + NL;
        }
        return data;
    }

    private void pokeFirstRow() {
        String where = "R1C2:R1C"+(columnIdList.size()+1);
        String what = getColumnIdsString();
        disableFirstRowListener();
        poke(where, what);
        enableFirstRowListener();
    }

    private String getColumnIdsString() {
        String data="";
        for (String s : columnIdList) {
            data += s + TAB;
        }
        return data;
    }

    private void pokeRow(int row, int count, String what) {
        if (row == 1) {
            System.out.println("Row 1 should never be poked to!");
            System.exit(-1);
        }
        poke("R" + row + "C2:R" + row + "C" + count, what);
    }

    private void poke(String where, String what) {
        while (true) {
            try {
                ddeLink.poke(where, what);
                return;
            } catch (DDELink.ConversationException e) {
//            e.printStackTrace();
                log.debug("Excel seems busy during write. waiting... (caught Exception: '" + e.getMessage() + "')");
                Tool.sleepFor(3000);
            }
        }
    }

    //todo: dont read and parse the full row but only the used part! depend on first row to find number of elements in lower rows
    private void parseFirstColumn(String data) {
        String firstColumnString = data.replace("\r\n", "\t").replace(" ","");
        log.info("dataLength:"+data.length());
//        log.info("firstColumnString="+firstColumnString.replace("\t",":"));
        String[] parts = firstColumnString.split("\t");
        ArrayList<String> oldRowIdList = rowIdList;
        rowIdList = new ArrayList<String>();
        mapOfRowIds.clear();
        rowIdList.addAll(Arrays.asList(parts));
        for (String s : rowIdList) mapOfRowIds.put(s, s);
//        if (rowIdList.size() > 0) rowIdList.remove(0);

        if(settingsThread.isAlive()) recordChangedRowIds(oldRowIdList);
    }

    private void recordChangedRowIds(ArrayList<String> oldRowIdList) {
        for(int i=0; i<oldRowIdList.size(); i++) {
            String oldId = oldRowIdList.get(i);
            boolean removedId = i>=rowIdList.size();
            if(removedId) {
                removedIdList.add(oldId);
                continue;
            }
            String newId = rowIdList.get(i);
            boolean changedId = (newId.compareTo(oldId)!=0);
            if(changedId) {
                removedIdList.add(oldId);
            }
        }
    }


    private void parseFirstRow(String firstRow) {
        String firstRowString = firstRow.replace("\r\n", "\t");
        String[] parts = firstRowString.split("\t");
        columnIdList.clear();
        mapOfColumnIds.clear();
        columnIdList.addAll(Arrays.asList(parts));
        for (String s : columnIdList) mapOfColumnIds.put(s, s);
//        if (columnIdList.size() > 0) columnIdList.remove(0);
    }

    private String readRow(int rowNumber) {
        while(true) {
            try {
                String rowString = ddeLink.request("R"+rowNumber);
                return rowString;
            } catch(DDELink.ConversationException e) {
                log.debug("Excel seems busy during read. waiting... (caught Exception: '"+e.getMessage()+"')");
                Tool.sleepFor(3000);
            }
        }
    }


    private static String NA = "n/a";
    private static String TAB = "\t";
    private static String NL = "\r\n";

    private ConcurrentHashMap<String,String> settingsRows;
    private boolean snapShotMode;
    private ConcurrentHashMap<String, MatrixItem> combiKey2ItemMap;
    private ArrayList<String> columnIdList;
    private ConcurrentHashMap<String,String> mapOfColumnIds;
    private ArrayList<String> rowIdList;
    private ConcurrentHashMap<String,String> mapOfRowIds;
    private final IProvideDDEConversation ddeLink;
    private String naString;
    private IConsumeAxisChanges listenerAxisChange;
    private Thread settingsThread;
    private boolean shutdown;
    private ISendBulkSettings settingsSender;
    private ConcurrentLinkedQueue<String> removedIdList;
    private boolean resendSettings;
    private String firstColumnExcelArray = "R2C1:R1000C1";
    private String firstRowExcelArray = "R1C2:R1C1000";

}
