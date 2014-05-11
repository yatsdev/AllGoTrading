package org.yats.trading;


import org.yats.common.Decimal;

import java.util.LinkedList;


//todo: allow for multiple currencies. currently different currencies completely ignored
//todo: all prices and sizes are in Decimals. need to be replaced by own classes, fixed decimal based, prices with currency
//todo: to calculate profit in a currency other than the one trades happened the historic exchange rate needs to be known!
//todo: since this is not feasible, profit has to be a list of profits in different currencies that is only converted at
//todo: current exchange rate to target currency
//todo: historic profits need to be available in their respective currencies and are converted at the current rates to target currency
public class ReceiptStorage implements IConsumeReceipt, IProvidePosition, IProvideProfit {

    @Override
    public void onReceipt(Receipt receipt) {
        if(receipt.isRejection()) return;
        receiptList.add(receipt);
        ProductAccountPosition positionChange = new ProductAccountPosition(receipt.getProductId(), receipt.getInternalAccount(), receipt.getPositionChange());
        positionSnapshotFromReceipts.add(positionChange);
    }

    public Decimal getInternalAccountPositionForProduct(String internalAccount, String productId)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    public Decimal getPositionForProduct(String productId)
    {
        throw new RuntimeException("Not implemented yet.");
    }

    public int getNumberOfReceipts() {
        throw new RuntimeException("Not implemented yet.");
    }

    public int getNumberOfReceiptsForInternalAccount(String internalAccount) {
        throw new RuntimeException("Not implemented yet.");
    }

    public String toCSV() {
        throw new RuntimeException("Not implemented yet.");
    }


    public static ReceiptStorage createFromCSV(String csv) {
        throw new RuntimeException("Not implemented yet.");
    }

    public void setPositionSnapshot(PositionSnapshot positionSnapshot) {
        this.positionSnapshot = positionSnapshot;
    }

    public void setProfitSnapshot(ProfitSnapshot profitSnapshot) {
        this.profitSnapshot = profitSnapshot;
    }


    public ReceiptStorage() {

        receiptList = new LinkedList<Receipt>();
        positionSnapshot = new PositionSnapshot();
        positionSnapshotFromReceipts = new PositionSnapshot();
        profitSnapshotFromReceipts = new ProfitSnapshot();
        profitSnapshot = new ProfitSnapshot();
    }


    LinkedList<Receipt> receiptList;

    // cumulated position changes from receipts
    private PositionSnapshot positionSnapshotFromReceipts;

    // snapshot of positions so far. for example end of day positions.
    private PositionSnapshot positionSnapshot;

    // snapshot of profits from receipts
    private ProfitSnapshot profitSnapshotFromReceipts;

    // snapshot of profits from receipts not included. e.g. end of day
    private ProfitSnapshot profitSnapshot;


} // class
