package org.yats.connectivity.excel;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.trading.ISendBulkSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SheetAccess implements DDELinkEventListener, Runnable {

    final Logger log = LoggerFactory.getLogger(SheetAccess.class);

    @Override
    public void onDisconnect() {
    }

    @Override
    public synchronized void onItemChanged(String sheetId, String cellId, String data) {
        if (cellId.compareTo("C1") == 0) {
            updateFirstColumn(data);
        } else if (cellId.compareTo("R1") == 0) {
            updateFirstRow(data);
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
        ConcurrentHashMap<String, String> rowIdsToUpdate = getRowIdsToUpdate(itemList);
        updateCombiKey2ItemMap(itemList);

        for (String p : rowIdsToUpdate.keySet()) {
            pokeRowForRowIds(p);
        }
    }

    public void init(String applicationName, String sheetName) throws DDELink.ConversationException {
        ddeLink.setTimeout(2000);
        ddeLink.connect(applicationName, sheetName);
        String firstColumnString = ddeLink.request("C1");
        parseFirstColumn(firstColumnString);
        String firstRowString = ddeLink.request("R1");
        parseFirstRow(firstRowString);
        ddeLink.startAdvice("C1");
        ddeLink.startAdvice("R1");
    }

    public void connect(String applicationname, String sheetname) {
        ddeLink.connect(applicationname, sheetname);
    }

    public void readFirstRowFromDDE() {
        String firstRow = ddeLink.request("R1");
        parseFirstRow(firstRow);
    }

    public void readFirstColumnFromDDE() {
        String firstColumn = ddeLink.request("C1");
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
        String s = "";
        int index = rowIdList.indexOf(rowId);
        if (index < 0) return;
        int row = 2 + index;
        int count = 1;
        for (String columnId : columnIdList) {
            count++;
            String key = MatrixItem.getKey(rowId, columnId);
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem ap = combiKey2ItemMap.get(key);
                s += ap.getData();
            } else s += naString;
            s += "\t";
        }
        pokePositionsRow(row, count, s);
    }

    private void updateCombiKey2ItemMap(Collection<MatrixItem> list) {
        for (MatrixItem pos : list) {
            String key = pos.getKey();
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem old = combiKey2ItemMap.get(key);
                if (pos.isSameAs(old)) continue;
            }
            combiKey2ItemMap.put(key, pos);
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
        pokePositions("C1", getRowIdsString());
    }

    private String getRowIdsString() {
        String data =  "\r\n";
        for (String s : rowIdList) {
            data += s + "\r\n";
        }
        return data;
    }

    private void pokeFirstRow() {
        pokePositions("R1", getColumnIdsString());
    }

    private String getColumnIdsString() {
        String data = "\t";
        for (String s : columnIdList) {
            data += s + "\t";
        }
        return data;
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
                ddeLink.poke(where, what);
                return;
            } catch (DDELink.ConversationException e) {
//            e.printStackTrace();
                log.debug("Excel seems busy during write. waiting... (caught Exception: '" + e.getMessage() + "')");
                Tool.sleepFor(3000);
            }
        }
    }

    private void parseFirstColumn(String data) {
        String firstColumnString = data.replace("\r\n", "\t");
        String[] parts = firstColumnString.split("\t");
        rowIdList.clear();
        mapOfRowIds.clear();
        rowIdList.addAll(Arrays.asList(parts));
        for (String s : rowIdList) mapOfRowIds.put(s, s);
        if (rowIdList.size() > 0) rowIdList.remove(0);
    }


    private void parseFirstRow(String firstRow) {
        String firstRowString = firstRow.replace("\r\n", "\t");
        String[] parts = firstRowString.split("\t");
        columnIdList.clear();
        mapOfColumnIds.clear();
        columnIdList.addAll(Arrays.asList(parts));
        for (String s : columnIdList) mapOfColumnIds.put(s, s);
        if (columnIdList.size() > 0) columnIdList.remove(0);
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


    private boolean resendSettings;
}
