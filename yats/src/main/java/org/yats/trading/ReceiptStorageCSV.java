package org.yats.trading;

import org.yats.common.FileTool;

public class ReceiptStorageCSV implements IConsumeReceipt {

    @Override
    public void onReceipt(Receipt receipt) {
        String receiptString = receipt.toStringCSV();
        FileTool.writeToTextFile(filename, receiptString, true);
    }

    public ReceiptStorageCSV(String filename) {
        this.filename = filename;
    }

    String filename;

} // class
