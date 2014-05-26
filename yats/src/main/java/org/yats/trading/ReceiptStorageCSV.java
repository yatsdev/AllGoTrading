package org.yats.trading;

import org.yats.common.FileTool;

public class ReceiptStorageCSV implements IConsumeReceipt {

    public static String FILENAME_DEFAULT = "ReceiptStorage.csv";

    @Override
    public void onReceipt(Receipt receipt) {
        String receiptString = receipt.toStringCSV();
        FileTool.writeToTextFile(filename, receiptString, true);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ReceiptStorageCSV() {
        this.filename = FILENAME_DEFAULT;
    }

    private String filename;

} // class
