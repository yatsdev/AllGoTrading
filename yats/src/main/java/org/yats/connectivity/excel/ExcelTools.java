package org.yats.connectivity.excel;


import java.util.Arrays;
import java.util.Vector;

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


}
