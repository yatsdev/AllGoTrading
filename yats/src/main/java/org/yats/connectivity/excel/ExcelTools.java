package org.yats.connectivity.excel;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelTools {

    public Vector R1ColumnParser(String R1){
        Vector<String> r1ColumnToVector;
        String[] parts = R1.split("\t");
        r1ColumnToVector = new Vector<String>(Arrays.asList(parts));
        if(r1ColumnToVector.size()>0&&!(r1ColumnToVector.elementAt(0).compareTo("\r\n")==0)) {
            r1ColumnToVector.removeElementAt(0);}//R1C1 is empty
        String lastElement = r1ColumnToVector.lastElement();
        String lastElement2 = lastElement.replace("\r\n", "");
        r1ColumnToVector.removeElementAt(r1ColumnToVector.size() - 1);
        r1ColumnToVector.add(lastElement2);

        if (r1ColumnToVector.lastElement().compareTo("") == 0) {
            r1ColumnToVector.remove("");
        }

        return r1ColumnToVector;
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


    private void parseFirstRow(String firstRow) {
        String accountsString = firstRow.replace("\r\n", "\t");
        String[] parts = accountsString.split("\t");
        keysInFirstRow.clear();
        mapOfFirstRowKeys.clear();
        keysInFirstRow.addAll(Arrays.asList(parts));
        for (String s : keysInFirstRow) mapOfFirstRowKeys.put(s, s);
        if (keysInFirstRow.size() > 0) keysInFirstRow.remove(0);
    }


    public ExcelTools(IProvideDDEConversation _DDELink) {
        ddeLink = _DDELink;
        keysInFirstRow = new ArrayList<String>();
        mapOfFirstRowKeys = new ConcurrentHashMap<String, String>();
    }

    private ArrayList<String> keysInFirstRow;
    private ConcurrentHashMap<String,String> mapOfFirstRowKeys;
    private final IProvideDDEConversation ddeLink;

}
