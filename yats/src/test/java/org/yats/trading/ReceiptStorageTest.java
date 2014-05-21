package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;
import org.yats.common.UniqueId;


public class ReceiptStorageTest {

    public static String filename = "canSerializeToCSVFileAndParseAgain.csv";
    private static String INTERNAL_ACCOUNT1 = "intAccount1";
    private static String INTERNAL_ACCOUNT2 = "intAccount2";
    PositionSnapshot positionSnapshot;
    ProfitSnapshot profitSnapshot;
    ReceiptStorage storage;
    Product product;
    Receipt receipt1;
    Receipt receipt2;
    Receipt receipt3;
    Receipt receipt4;
    Receipt receipt5;


    //    @Test
//    public void canCalculateProductProfitForInternalAccountWithSnapshot()
//    {
//        int profitWithSnapshot = (int)storage.getInternalAccountProfitForProduct(INTERNAL_ACCOUNT1, product.getProductId());
//        assert (profitWithSnapshot == -2 -2 -2 -5);
//
//    }


    @Test
    public void canProcessReceipts() {
        assert (storage.getNumberOfReceipts() == 5);
        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1) == 4);
        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT2) == 1);
    }

    @Test
    public void canCalculateCurrentProductPositionOverAllInternalAccounts() {
        int productPositionGlobal = storage.getPositionForProduct(product.getProductId()).toInt();
        assert (productPositionGlobal == (+1 + 1 + 1 + 9 - 2));

        // maybe your problems came from the minus sign in the receipt number 5 for total traded size and current traded size
        // also, it might be better to have braces around the sum in the assert, like above now.
        // now works fine

    }

    @Test
    public void canCalculateProductPositionForInternalAccount()
    {
        int productPositionAccount1 = storage.getPosition(new PositionRequest(INTERNAL_ACCOUNT1, product.getProductId())).toInt();
        assert (productPositionAccount1 == +1 + 1 + 1 - 2);
        int productPositionAccount2 = storage.getPosition(new PositionRequest(INTERNAL_ACCOUNT2, product.getProductId())).toInt();
        assert (productPositionAccount2 == 9);
    }

    @Test
    public void canSerializeToCSVAndParseAgain()
    {
        String csv = storage.toStringCSV();
        ReceiptStorage newStorage = ReceiptStorage.createFromCSV(csv);
        String newCSV = newStorage.toStringCSV();
        assert (csv.compareTo(newCSV) == 0);
        assert (newStorage.getNumberOfReceipts() == 5);
        assert (newStorage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1) == 4);
    }

    @Test
    public void canSerializeToCSVFileAndParseAgain()
    {
        String csv = storage.toStringCSV();
        FileTool.writeToTextFile(filename, csv, false);
        String csvFromFile = FileTool.readFromTextFile(filename);
        FileTool.deleteFile(filename);
        ReceiptStorage newStorage = ReceiptStorage.createFromCSV(csvFromFile);
        String newCSV = newStorage.toStringCSV();
        assert (csv.compareTo(newCSV) == 0);
        assert (newStorage.getNumberOfReceipts() == 5);
        assert (newStorage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1) == 4);
    }

    @Test
    public void canCalculateProductPositionForInternalAccountWithSnapshot() {
        storage.setPositionSnapshot(positionSnapshot);
        int productPositionWithSnapshot = storage.getPosition(new PositionRequest(INTERNAL_ACCOUNT1, product.getProductId())).toInt();
        System.out.println(productPositionWithSnapshot);

       assert (productPositionWithSnapshot ==  (+1 +1 +1 -2 +10));
    }

    @BeforeMethod
    public void setUp() {
        storage = new ReceiptStorage();
        product = new Product("product1", "sym1", "exch1");
        receipt1 = Receipt.create()
                .withOrderId(UniqueId.createFromString("1"))
                .withProductId(product.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.ONE)
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt2 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProductId(product.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.ONE)
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ONE)
                .withBookSide(BookSide.BID)
        ;
        receipt3 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProductId(product.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.ONE)
                .withTotalTradedSize(Decimal.fromDouble(2))
                .withPrice(Decimal.fromDouble(50))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt4 = Receipt.create()
                .withOrderId(UniqueId.createFromString("4"))
                .withProductId(product.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT2)
                .withCurrentTradedSize(Decimal.fromDouble(9))
                .withTotalTradedSize(Decimal.fromDouble(9))
                .withPrice(Decimal.fromDouble(87))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.BID)
        ;
        receipt5 = Receipt.create()
                .withOrderId(UniqueId.createFromString("5"))
                .withProductId(product.getProductId())
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(Decimal.fromDouble(-2))
                .withTotalTradedSize(Decimal.fromDouble(-2))
                .withPrice(Decimal.fromDouble(48))
                .withResidualSize(Decimal.ZERO)
                .withBookSide(BookSide.ASK)
        ;

        processReceipts();
        positionSnapshot = new PositionSnapshot();
        positionSnapshot.add(new ProductAccountPosition(product.getProductId(), INTERNAL_ACCOUNT1, Decimal.fromDouble(10)));
//        profitSnapshot = new ProfitSnapshot();
//        profitSnapshot.add(new ProductAccountProfit(product.getProductId(), INTERNAL_ACCOUNT1, Decimal.fromDouble(-5)));
    }

    private void processReceipts() {
        storage.onReceipt(receipt1);
        storage.onReceipt(receipt2);
        storage.onReceipt(receipt3);
        storage.onReceipt(receipt4);
        storage.onReceipt(receipt5);
    }
} // class