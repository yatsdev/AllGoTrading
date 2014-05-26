package org.yats.trading;

import org.yats.common.Decimal;

import java.util.LinkedList;

public class ReceiptStorageMem implements IConsumeReceipt, IProvidePosition, IProvideProfit {

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

    @Override
    public void onReceipt(Receipt receipt) {
        if (receipt.isRejection()) return;
        receiptList.add(receipt);
    }


    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
        AccountPosition position = new AccountPosition(positionRequest.getProductId(), Decimal.ZERO, positionRequest.getAccount());
        for (Receipt receipt : receiptList) {
            if(positionRequest.isForReceipt(receipt)) {
                position = position.add(receipt);
            }
        }
        return position;
    }

    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId) {

        throw new RuntimeException("Not implemented yet.");

    }

    public Position getPositionForProduct(String productId) {
        Position p = new Position(productId, Decimal.ZERO);
        for (Receipt receipt : receiptList) {
            if (receipt.hasProductId(productId)) {
                p = p.add(receipt);
            }
        }
        return p;
    }

    public int getNumberOfReceipts() {
        return receiptList.size();
    }


    public ReceiptStorageMem() {
        receiptList = new LinkedList<Receipt>();
    }


    private LinkedList<Receipt> receiptList;

} // class
