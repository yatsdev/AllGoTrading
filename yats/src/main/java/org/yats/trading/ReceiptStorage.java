//package org.yats.trading;
//
//
//import org.yats.common.Decimal;
//
//import java.util.LinkedList;
//
//
////todo: allow for multiple currencies. currently different currencies completely ignored
////todo: to calculate profit in a currency other than the one trades happened the historic exchange rate needs to be known!
////todo: since this is not feasible, profit has to be a list of profits in different currencies that is only converted at
////todo: current exchange rate to target currency
////todo: historic profits need to be available in their respective currencies and are converted at the current rates to target currency
//
//
//@Deprecated
//public class  ReceiptStorage implements IConsumeReceipt {
//
//    @Override
//    public void onReceipt(Receipt receipt) {
//    }
//
////    public String toStringCSV() {
////        String lineSeparator = System.getProperty( "line.separator" );
////        StringBuffer b = new StringBuffer();
////        for (Receipt r : receiptList) {
////            String receiptString = r.toStringCSV();
////            b.append(receiptString);
////            b.append(lineSeparator);
////        }
////        return b.toString();
////    }
////
////
////    public static ReceiptStorage createFromCSV(String csv) {
////
////        String lineSeparator = System.getProperty( "line.separator" );
////        String[] lines = csv.split(lineSeparator);
////        ReceiptStorage storage = new ReceiptStorage();
////        for(int i = 0; i<lines.length; i++) {
////            String line = lines[i];
////            Receipt r = Receipt.fromStringCSV(line);
////            storage.onReceipt(r);
////        }
////        return storage;
////    }
////
////
////
////    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
////        AccountPosition position = new AccountPosition(positionRequest.getProductId(), Decimal.ZERO, positionRequest.getAccount());
////        for (Receipt receipt : receiptList) {
////            if(positionRequest.isForReceipt(receipt)) {
////                position = position.add(receipt);
////            }
////        }
////        return position;
////    }
////
////    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId) {
////
////        throw new RuntimeException("Not implemented yet.");
////
////    }
////
////    public Position getPositionForProduct(String productId) {
////        Position p = new Position(productId, Decimal.ZERO);
////        for (Receipt receipt : receiptList) {
////            if (receipt.hasProductId(productId)) {
////                p = p.add(receipt);
////            }
////        }
////        return p;
////    }
////
////    public int getNumberOfReceipts() {
////        return numberOfReceipts;
////    }
////
//////    public int getNumberOfReceiptsForInternalAccount(String internalAccount) {
//////
//////        int NumberOfReceiptsForInternalAccount = 0;
//////        Receipt receipt;
//////        for (int i = 0; i < receiptList.size(); i++) {
//////            receipt = receiptList.get(i);
//////            if (receipt.getInternalAccount().compareTo(internalAccount) == 0) {
//////                NumberOfReceiptsForInternalAccount = NumberOfReceiptsForInternalAccount + 1;
//////            }
//////        }
//////        return NumberOfReceiptsForInternalAccount;
//////    }
////
////    public ReceiptStorage() {
////    }
////
////
////    public void setPositionSnapshot(PositionSnapshot positionSnapshot) {
////        this.positionSnapshot = positionSnapshot;
////    }
////
////    public void setProfitSnapshot(ProfitSnapshot profitSnapshot) {
////        this.profitSnapshot = profitSnapshot;
////    }
////
////    private int numberOfReceipts = 0;
////
////    private LinkedList<Receipt> receiptList;
////    // cumulated position changes from receipts
////    private PositionSnapshot positionSnapshotFromReceipts;
////    // snapshot of positions so far. for example end of day positions.
////    private PositionSnapshot positionSnapshot;
////    // snapshot of profits from receipts
////    private ProfitSnapshot profitSnapshotFromReceipts;
////    // snapshot of profits from receipts not included. e.g. end of day
////    private ProfitSnapshot profitSnapshot;
////
//
//
//} // class
