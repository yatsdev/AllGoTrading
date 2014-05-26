package org.yats.trading;

import org.yats.common.Decimal;

import java.util.LinkedList;

public class ReceiptStorageMem implements IConsumeReceipt {

    @Override
    public void onReceipt(Receipt receipt) {
        receiptList.add(receipt);
    }

    public String toStringCSV() {
        String lineSeparator = System.getProperty( "line.separator" );
        StringBuffer b = new StringBuffer();
        for (Receipt r : receiptList) {
            String receiptString = r.toStringCSV();
            b.append(receiptString);
            b.append(lineSeparator);
        }
        return b.toString();
    }

    public static ReceiptStorageMem fromStringCSV(String csv) {

        String lineSeparator = System.getProperty( "line.separator" );
        String[] lines = csv.split(lineSeparator);
        ReceiptStorageMem storage = new ReceiptStorageMem();
        for(int i = 0; i<lines.length; i++) {
            String line = lines[i];
            Receipt r = Receipt.fromStringCSV(line);
            storage.onReceipt(r);
        }
        return storage;
    }

    public int getNumberOfReceipts() {
        return receiptList.size();
    }

    public ReceiptStorageMem() {
        receiptList = new LinkedList<Receipt>();
    }

    private LinkedList<Receipt> receiptList;

} // class
