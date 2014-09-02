package org.yats.trading;

import org.yats.common.FileTool;
import org.yats.common.IProvideProperties;

public class ReceiptStorageCSV implements IConsumeReceipt {

    @Override
    public void onReceipt(Receipt receipt) {
        String receiptString = receipt.toStringCSV();
        FileTool.writeToTextFile(receiptsFilename, receiptString + FileTool.getLineSeparator(), true);
        if(receipt.isTrade())
            FileTool.writeToTextFile(transactionsFilename, receiptString + FileTool.getLineSeparator(), true);
    }

    public ReceiptStorageCSV(IProvideProperties _prop) {

        this.receiptsFilename = _prop.get("filenameReceipts");
        this.transactionsFilename = _prop.get("filenameTransactions");
    }

    private String receiptsFilename;
    private String transactionsFilename;

} // class
