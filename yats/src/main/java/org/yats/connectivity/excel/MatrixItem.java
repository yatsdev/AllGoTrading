package org.yats.connectivity.excel;

public class MatrixItem {

    public String getRowId() {
        return rowId;
    }

    public String getColumnId() {
        return columnId;
    }

    public String getData() {
        return data;
    }

    public String getKey() {
        return getKey(rowId,columnId);
    }

    public static String getKey(String r, String c) {
        return r+";"+c;
    }

    public boolean isSameAs(MatrixItem old) {
        return rowId.compareTo(old.rowId) ==0
                && columnId.compareTo(old.columnId)==0
                && data.compareTo(old.data)==0;
    }

    public MatrixItem(String rowId, String columnId, String data) {

        this.rowId = rowId;
        this.columnId = columnId;
        this.data = data;
    }

    private final String rowId;
    private final String columnId;
    private final String data;
}
