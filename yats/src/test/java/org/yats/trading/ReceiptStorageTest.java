package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;


public class ReceiptStorageTest {

    public static String filename = "canSerializeToCSVFileAndParseAgain.csv";

//    @Test(groups = { "integration", "inMemory" })
//    public void canCalculateProductProfitForInternalAccountWithSnapshot()
//    {
//        int valueWithSnapshot = (int)storage.getValueForAccountProduct(INTERNAL_ACCOUNT1, product.getProductId());
//        assert (profitWithSnapshot == -2 -2 -2 -5);
//
//    }
//
//    @Test(groups = { "integration", "inMemory" })
//    public void canCalculateCurrentProductPositionOverAllInternalAccounts() {
//        Position p = storage.getPositionForProduct(product.getProductId());
//        assert (p.isSize(+1 +1 +1 +9 -2));
//    }
//
//    @Test(groups = { "integration", "inMemory" })
//    public void canCalculateProductPositionForInternalAccount()
//    {
//        Position positionAccount1 = storage.getAccountPosition(new PositionRequest(INTERNAL_ACCOUNT1, product.getProductId()));
//        assert (positionAccount1.isSize(+1 +1 +1 -2));
//        Position positionAccount2 = storage.getAccountPosition(new PositionRequest(INTERNAL_ACCOUNT2, product.getProductId()));
//        assert (positionAccount2.isSize(9));
//    }
//
//    @Test(groups = { "integration", "inMemory" })
//    public void canCalculateProductPositionForInternalAccountWithSnapshot() {
//        storage.setPositionSnapshot(positionSnapshot);
//        Position productPositionWithSnapshot = storage.getAccountPosition(positionRequest1);
////        assert(productPositionWithSnapshot.isSize(+1 + 1 + 1 -2 +10));
//    }

    @Test(groups = { "integration", "inMemory" })
    public void canProcessReceipts() {
        assert (storage.getNumberOfReceipts() == 5);
    }

    @Test(groups = { "integration", "inMemory" })
    public void canSerializeToCSVStringAndParseAgain()
    {
        String csv = storage.toStringCSV();
        ReceiptStorageMem newStorage = ReceiptStorageMem.fromStringCSV(csv);
        String newCSV = newStorage.toStringCSV();
        assert (csv.compareTo(newCSV) == 0);
        assert (newStorage.getNumberOfReceipts() == 5);
    }

    @Test(groups = { "integration", "inMemory" })
    public void canSerializeToCSVFileAndParseAgain()
    {
        String csv = storage.toStringCSV();
        FileTool.writeToTextFile(filename, csv, false);
        String csvFromFile = FileTool.readFromTextFile(filename);
        FileTool.deleteFile(filename);
        ReceiptStorageMem newStorage = ReceiptStorageMem.fromStringCSV(csvFromFile);
        String newCSV = newStorage.toStringCSV();
        assert (csv.compareTo(newCSV) == 0);
        assert (newStorage.getNumberOfReceipts() == 5);
    }
    
    @Test(groups = { "integration", "inMemory" })
    public void canSerializeToCSVFileAndParseAgainWithABigDecimalPrice()
    {
        Receipt receiptBidDecimal = Receipt.create()
                .withBookSide(BookSide.BID)
                .withPrice(new Decimal("1.572690342360756259580991313234512"));
        storage.onReceipt(receiptBidDecimal);
        String csv = storage.toStringCSV();
        ReceiptStorageMem newStorage = ReceiptStorageMem.fromStringCSV(csv);
        String newCSV = newStorage.toStringCSV();
        assert (csv.compareTo(newCSV) == 0);
        assert (newStorage.getNumberOfReceipts() == 6);
    }


    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setUp() {
        storage = new ReceiptStorageMem();
        processReceipts();
    }

    private void processReceipts() {
        storage.onReceipt(ReceiptTest.RECEIPT1);
        storage.onReceipt(ReceiptTest.RECEIPT2);
        storage.onReceipt(ReceiptTest.RECEIPT3);
        storage.onReceipt(ReceiptTest.RECEIPT4);
        storage.onReceipt(ReceiptTest.RECEIPT5);
    }

    private static String INTERNAL_ACCOUNT1 = "intAccount1";
    private static String INTERNAL_ACCOUNT2 = "intAccount2";
    ReceiptStorageMem storage;

} // class
