package org.yats.connectivity.excel;


import org.yats.common.Tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelTools implements DDELinkEventListener {

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onItemChanged(String sheetId, String cellId, String data) {
        if (cellId.compareTo("C1") == 0) {
            updateFirstColumn(data);
        } else if (cellId.compareTo("R1") == 0) {
            updateFirstRow(data);
        }
    }

    private void updateFirstRow(String data) {
        String currentFirstRowString = getColumnIdsString();
        parseFirstRow(data);
        String newFirstRowString = getColumnIdsString();
        boolean firstRowChanged = currentFirstRowString.compareTo(newFirstRowString)!=0;
        if(firstRowChanged) pokeFirstRow();
    }

    private void updateFirstColumn(String data) {
        String currentFirstColumnString = getRowIdsString();
        parseFirstColumn(data);
        String newFirstColumnString = getRowIdsString();
        boolean firstColumnChanged = currentFirstColumnString.compareTo(newFirstColumnString)!=0;
        if(firstColumnChanged) pokeFirstColumn();
    }


//    @Override
//    public synchronized void onPositionSnapshot(PositionSnapshot snapshot) {
//        updatePositionsAxis(snapshot);
//        productAccount2PositionMap.clear();
//        ConcurrentHashMap<String, String> productsToUpdate = getProductsToUpdate(snapshot);
//        updateProductAccount2PositionMap(snapshot);
//
//        for (String p : productsToUpdate.keySet()) {
//            pokeRowForAllAccountsOfProduct(p);
//        }
//    }

    public void disconnect() {
        ddeLink.disconnect();
    }

    public void updateMatrix(List<MatrixItem> itemList) {
        updatePositionsAxis(itemList);
        if(snapShotMode) combiKey2ItemMap.clear();
        ConcurrentHashMap<String, String> rowIdsToUpdate = getRowIdsToUpdate(itemList);
        updateCombiKey2ItemMap(itemList);

        for (String p : rowIdsToUpdate.keySet()) {
            pokeRowForRowIds(p);
        }
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
            } else s += "n/a";
            s += "\t";
        }
        pokePositionsRow(row, count, s);
    }

    private void updateCombiKey2ItemMap(List<MatrixItem> list) {
        for (MatrixItem pos : list) {
            String key = pos.getKey();
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem old = combiKey2ItemMap.get(key);
                if (pos.isSameAs(old)) continue;
            }
            combiKey2ItemMap.put(key, pos);
        }
    }

    private ConcurrentHashMap<String, String> getRowIdsToUpdate(List<MatrixItem> list) {
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

    private void updatePositionsAxis(List<MatrixItem> itemList) {
        String firstRowStringOld = getColumnIdsString();
        for (MatrixItem p : itemList) {
            updateList(columnIdList, mapOfColumnIds, p.getColumnId());
        }
        boolean updateFirstRow = firstRowStringOld.compareTo(getColumnIdsString()) != 0;
        if (updateFirstRow) pokeFirstRow();

        String firstColumnStringOld = getRowIdsString();
        for (MatrixItem p : itemList) {
            updateList(rowIdList, mapOfRowIds, p.getRowId());
        }
        boolean updateFirstColumn = firstColumnStringOld.compareTo(getRowIdsString()) != 0;
        if (updateFirstColumn) pokeFirstColumn();
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
        String data = "\r\n";
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
                System.out.println("Excel seems busy. waiting...");
                Tool.sleepFor(3000);
            }
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



//    private void parsePositionAccounts(String data) {
//        String accountsString = data.replace("\r\n", "\t");
//        String[] parts = accountsString.split("\t");
//        positionAccounts.clear();
//        positionAccountsMap.clear();
//        positionAccounts.addAll(Arrays.asList(parts));
//        for (String s : positionAccounts) positionAccountsMap.put(s, s);
//        if (positionAccounts.size() > 0) positionAccounts.remove(0);
//    }


    //    private void parseFirstRow(String data) {
//        String accountsString = data.replace("\r\n", "\t");
//        String[] parts = accountsString.split("\t");
//        positionAccounts.clear();
//        positionAccountsMap.clear();
//        positionAccounts.addAll(Arrays.asList(parts));
//        for (String s : positionAccounts) positionAccountsMap.put(s, s);
//        if (positionAccounts.size() > 0) positionAccounts.remove(0);
//    }

//    private ArrayList<String> positionProducts;
//    private ConcurrentHashMap<String, String> positionProductsMap;
//    private ArrayList<String> positionAccounts;
//    private ConcurrentHashMap<String, String> positionAccountsMap;
    //    private DDEClientConversation conversationPositions;

//    private IProvideDDEConversation conversation;
//    private ConcurrentHashMap<String, AccountPosition> productAccount2PositionMap;


    public void setSnapShotMode(boolean snapShotMode) {
        this.snapShotMode = snapShotMode;
    }

    public ExcelTools(IProvideDDEConversation _DDELink) {
        ddeLink = _DDELink;
        ddeLink.setEventListener(this);
        columnIdList = new ArrayList<String>();
        mapOfColumnIds = new ConcurrentHashMap<String, String>();
        rowIdList = new ArrayList<String>();
        mapOfRowIds = new ConcurrentHashMap<String, String>();
        combiKey2ItemMap = new ConcurrentHashMap<String, MatrixItem>();
        snapShotMode=true;
    }

    private boolean snapShotMode;
    private ConcurrentHashMap<String, MatrixItem> combiKey2ItemMap;
    private ArrayList<String> columnIdList;
    private ConcurrentHashMap<String,String> mapOfColumnIds;
    private ArrayList<String> rowIdList;
    private ConcurrentHashMap<String,String> mapOfRowIds;
    private final IProvideDDEConversation ddeLink;

}
