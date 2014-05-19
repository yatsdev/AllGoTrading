package org.yats.trading;


import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.joda.time.DateTime;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;

import java.io.*;
import java.util.LinkedList;


//todo: allow for multiple currencies. currently different currencies completely ignored
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

      public Decimal getInternalAccountPositionForProduct(String internalAccount, String productId) { 
        Decimal AccountPositionForProduct = Decimal.ZERO;
        Receipt receipt;
        for (int i = 0; i < receiptList.size(); i++) {

            receipt = receiptList.get(i);
            if (receipt.getInternalAccount().compareTo(internalAccount) == 0 && receipt.hasProductId(productId)) {

                AccountPositionForProduct = receipt.getCurrentTradedSize().add(AccountPositionForProduct);

            }
        }
        return AccountPositionForProduct;
    }

    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId)
    {
        throw new RuntimeException("Not implemented yet.");
    }

     public Decimal getPositionForProduct(String productId) { 
        Decimal PositionForProduct = Decimal.ZERO;
        Receipt receipt;
        for (int i = 0; i < receiptList.size(); i++) {

            receipt = receiptList.get(i);
            if (receipt.hasProductId(productId)) {
                PositionForProduct = receipt.getCurrentTradedSize().add(PositionForProduct);
            }
        }
        return PositionForProduct;
    }

    public int getNumberOfReceipts() {
        int NumberOfReceipts = 0;
        NumberOfReceipts = receiptList.size();
        return NumberOfReceipts;
    }
    
    
    public int getNumberOfReceiptsForInternalAccount(String internalAccount) {

        int NumberOfReceiptsForInternalAccount = 0;
        Receipt receipt;
        for (int i = 0; i < receiptList.size(); i++) {
            receipt = receiptList.get(i);
            if (receipt.getInternalAccount().compareTo(internalAccount) == 0) {
                NumberOfReceiptsForInternalAccount = NumberOfReceiptsForInternalAccount + 1;
            }
        }
        return NumberOfReceiptsForInternalAccount;
    }

       public String toCSV() {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("ReceiptStorage.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter writer = new CSVWriter(out);

        String[] toCSVfromReceipt = new String[0];

        for (int i = 0; i < receiptList.size(); i++) {

             toCSVfromReceipt = new String[]{receiptList.get(i).getTimestamp().toString(), receiptList.get(i).getOrderId().toString(), receiptList.get(i).getInternalAccount().toString(), receiptList.get(i).getExternalAccount().toString(), receiptList.get(i).getProductId().toString(), receiptList.get(i).getBookSide().toDirection() + "", receiptList.get(i).getResidualSize().toString(), receiptList.get(i).getCurrentTradedSize().toString(), receiptList.get(i).getTotalTradedSize().toString(), receiptList.get(i).getPrice().toString(), receiptList.get(i).getRejectReason().toString(), String.valueOf(receiptList.get(i).isEndState())}; //Attention to endstate
            writer.writeNext(toCSVfromReceipt);
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return toCSVfromReceipt[0].concat(toCSVfromReceipt[1]).concat(toCSVfromReceipt[2]).concat(toCSVfromReceipt[3]).concat(toCSVfromReceipt[4]).concat(toCSVfromReceipt[5]).concat(toCSVfromReceipt[6]).concat(toCSVfromReceipt[7]).concat(toCSVfromReceipt[8]).concat(toCSVfromReceipt[9]).concat(toCSVfromReceipt[10]).concat(toCSVfromReceipt[11]); //A string representation of the last Receipt of the Storage
    }


      public static ReceiptStorage createFromCSV(String csv) {


        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csv));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        String[] nextLine;
        ReceiptStorage storage = new ReceiptStorage();

        try {
            while ((nextLine = reader.readNext()) != null) {
                Receipt receiptFromCSV = new Receipt();
                receiptFromCSV.setTimestamp(DateTime.parse(nextLine[0]));
                receiptFromCSV.setOrderId(UniqueId.createFromString(nextLine[1]));
                receiptFromCSV.setInternalAccount(nextLine[2]);
                receiptFromCSV.setExternalAccount(nextLine[3]);
                receiptFromCSV.setProductId(nextLine[4]);
                receiptFromCSV.setBookSide(BookSide.fromDirection(Integer.parseInt(nextLine[5])));
                receiptFromCSV.setResidualSize(new Decimal(nextLine[6]));//Now I read them as Strings
                receiptFromCSV.setCurrentTradedSize(new Decimal(nextLine[7]));
                receiptFromCSV.setTotalTradedSize(new Decimal(nextLine[8]));
                receiptFromCSV.setPrice(new Decimal(nextLine[9]));
                receiptFromCSV.setRejectReason((nextLine[10]));
                receiptFromCSV.setEndState(Boolean.valueOf(nextLine[11]));
                storage.receiptList.add(receiptFromCSV);

            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return storage;

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
