package org.yats.trading;

import org.yats.common.FileTool;

public class ReceiptStorageCSV implements IConsumeReceipt {

    public static String FILENAME_RECEIPTS_DEFAULT = "Receipts";
    public static String FILENAME_TRANSACTIONS_DEFAULT = "Transactions";
    public static String FILENAME_EXTENSION = ".csv";

    @Override
    public void onReceipt(Receipt receipt) {
        String receiptString = receipt.toStringCSV();
        FileTool.writeToTextFile(receiptsFilename, receiptString + FileTool.getLineSeparator(), true);
        if(receipt.isTrade())
            FileTool.writeToTextFile(transactionsFilename, receiptString + FileTool.getLineSeparator(), true);
    }

    public void setReceiptsFilename(String filename) {
        this.receiptsFilename = filename;
    }

    public void setTransactionsFilename(String transactionsFilename) {
        this.transactionsFilename = transactionsFilename;
    }

    public ReceiptStorageCSV() {

        this.receiptsFilename = FILENAME_RECEIPTS_DEFAULT+FILENAME_EXTENSION;
        this.transactionsFilename = FILENAME_TRANSACTIONS_DEFAULT+FILENAME_EXTENSION;
    }

    private String receiptsFilename;
    private String transactionsFilename;

} // class
