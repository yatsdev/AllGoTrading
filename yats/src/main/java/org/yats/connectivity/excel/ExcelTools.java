package org.yats.connectivity.excel;


import org.yats.common.Tool;
import org.yats.trading.AccountPosition;
import org.yats.trading.PositionSnapshot;

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
        if (cellId.compareTo("C1") == 0) parseFirstColumn(data);
        if (cellId.compareTo("R1") == 0) parseFirstRow(data);
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
        combiKey2ItemMap.clear();
        ConcurrentHashMap<String, String> productsToUpdate = getRowIdsToUpdate(itemList);
        updateProductAccount2PositionMap(itemList);

        for (String p : productsToUpdate.keySet()) {
            pokeRowForAllAccountsOfProduct(p);
        }
    }

    private void pokeRowForAllAccountsOfProduct(String p) {
        String s = "";
        int index = keysInFirstColumn.indexOf(p);
        if (index < 0) return;
        int row = 2 + index;
        int count = 1;
        for (String account : keysInFirstRow) {
            count++;
            String key = AccountPosition.getKey(p, account);
            if (combiKey2ItemMap.containsKey(key)) {
                MatrixItem ap = combiKey2ItemMap.get(key);
                s += ap.getData();
            } else s += "n/a";
            s += "\t";
        }
        pokePositionsRow(row, count, s);
    }

    private void updateProductAccount2PositionMap(List<MatrixItem> list) {
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
        int originalPositionProductsSize = keysInFirstColumn.size();

        for (MatrixItem p : itemList) {
            updateList(keysInFirstColumn, mapOfFirstColumnKeys, p.getColumnId());
        }
        int originalPositionAccountsSize = keysInFirstRow.size();
        for (MatrixItem p : itemList) {
            updateList(keysInFirstRow, mapOfFirstRowKeys, p.getRowId());
        }
        boolean updateProductsColumn = keysInFirstColumn.size() > originalPositionProductsSize;
        if (updateProductsColumn) pokeFirstColumn();
        boolean updateAccountsColumn = keysInFirstRow.size() > originalPositionAccountsSize;
        if (updateAccountsColumn) pokeFirstRow();
    }

    private void updateList(List<String> list, ConcurrentHashMap<String, String> map, String item) {
        if (map.containsKey(item)) return;
        list.add(item);
        map.put(item, item);
    }

    private void pokeFirstColumn() {
        String data = "\r\n";
        for (String s : keysInFirstColumn) {
            data += s + "\r\n";
        }
        pokePositions("C1", data);
    }

    private void pokeFirstRow() {
        String data = "\t";
        for (String s : keysInFirstRow) {
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
        String productIdsString = data.replace("\r\n", "\t");
        String[] parts = productIdsString.split("\t");
        keysInFirstColumn.clear();
        mapOfFirstColumnKeys.clear();
        keysInFirstColumn.addAll(Arrays.asList(parts));
        for (String s : keysInFirstColumn) mapOfFirstColumnKeys.put(s, s);
        if (keysInFirstColumn.size() > 0) keysInFirstColumn.remove(0);
    }


    private void parseFirstRow(String firstRow) {
        String accountsString = firstRow.replace("\r\n", "\t");
        String[] parts = accountsString.split("\t");
        keysInFirstRow.clear();
        mapOfFirstRowKeys.clear();
        keysInFirstRow.addAll(Arrays.asList(parts));
        for (String s : keysInFirstRow) mapOfFirstRowKeys.put(s, s);
        if (keysInFirstRow.size() > 0) keysInFirstRow.remove(0);
    }



    public void connect(String applicationname, String sheetname) {
        ddeLink.connect(applicationname, sheetname);
    }

    public void readFirstRowFromDDE() {
        String firstRow = ddeLink.request("R1");
        parseFirstRow(firstRow);
    }

    public int countKeysInFirstRow(){
        int count=0;
        for(String s : keysInFirstRow) {
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
    private ConcurrentHashMap<String, MatrixItem> combiKey2ItemMap;

    public ExcelTools(IProvideDDEConversation _DDELink) {
        ddeLink = _DDELink;
        ddeLink.setEventListener(this);
        keysInFirstRow = new ArrayList<String>();
        mapOfFirstRowKeys = new ConcurrentHashMap<String, String>();
        keysInFirstColumn = new ArrayList<String>();
        mapOfFirstColumnKeys = new ConcurrentHashMap<String, String>();
        combiKey2ItemMap = new ConcurrentHashMap<String, MatrixItem>();
    }

    private ArrayList<String> keysInFirstRow;
    private ConcurrentHashMap<String,String> mapOfFirstRowKeys;
    private ArrayList<String> keysInFirstColumn;
    private ConcurrentHashMap<String,String> mapOfFirstColumnKeys;
    private final IProvideDDEConversation ddeLink;

}
