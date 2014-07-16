package org.yats.connectivity.excel;

public class ExcelConnection implements Runnable {


    public boolean isExcelRunning() {
        return false;
    }

    public boolean isWorkbookOpenInExcel() {
        return false;
    }

    public void startExcelWithWorkbook() {
    }

    public void startExcelLink() {
        thread.start();
    }

    @Override
    public void run() {
        // communicate with Excel here
    }

    public ExcelConnection(String excelFileName) {
        this.excelFileName = excelFileName;
        thread = new Thread(this);
    }

    private String excelFileName;
    private Thread thread;
} // class
