package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.UniqueId;

public class ReceiptStorageTest {

    private static String INTERNAL_ACCOUNT1 = "intAccount1";
    private static String INTERNAL_ACCOUNT2 = "intAccount2";

    @Test
    public void canProcessReceipts()
    {
        assert (storage.getNumberOfReceipts() == 4);
        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1) == 3);
        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT2) == 1);
    }

    @Test
    public void canCalculateCurrentProductPositionOverAllInternalAccounts()
    {
        int productPositionGlobal = (int)storage.getPositionForProduct(product);
        assert (productPositionGlobal == +1 +1 +1 +9 -2 +10);
    }

    @Test
    public void canCalculateProductPositionForInternalAccount()
    {
        int productPositionAccount1 = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT1, product);
        assert (productPositionAccount1 == +1 +1 +1 -2);
        int productPositionAccount2 = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT2, product);
        assert (productPositionAccount2 == 9);
    }

    @Test
    public void canSerializeAndParseItAgain() // CSV is only an example JSON, XML or XLS are fine too.
    {
        String csv = storage.toCSV();
        ReceiptStorage newStorage = ReceiptStorage.createFromCSV(csv);
        String newCSV = newStorage.toCSV();
        assert(csv.compareTo(newCSV) == 0);
        assert(newStorage.getNumberOfReceipts()==4);
        assert(newStorage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1)==3);
    }

    @Test
    public void canCalculateProductPositionForInternalAccountWithSnapshot()
    {
        storage.setPositionSnapshot(positionSnapshot);
        int productPositionWithSnapshot = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT1, product);
        assert (productPositionWithSnapshot == +1 +1 +1 -2 +10);
    }

    @Test
    public void canCalculateProductProfitForInternalAccountWithSnapshot()
    {
        int profitWithSnapshot = (int)storage.getInternalAccountProfitForProduct(INTERNAL_ACCOUNT1, product);
        assert (profitWithSnapshot == -2 -2 -2 -5);

    }

    @BeforeMethod
    public void setUp() {
        product = new Product("product1", "sym1", "exch1");
        receipt1 = Receipt.create()
                .withOrderId(UniqueId.createFromString("1"))
                .withProduct(product)
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(1)
                .withTotalTradedSize(1)
                .withPrice(50)
                .withResidualSize(0)
                ;
        receipt2 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProduct(product)
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(1)
                .withTotalTradedSize(1)
                .withPrice(50)
                .withResidualSize(1)
                ;
        receipt3 = Receipt.create()
                .withOrderId(UniqueId.createFromString("2"))
                .withProduct(product)
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(1)
                .withTotalTradedSize(2)
                .withPrice(50)
                .withResidualSize(0)
                ;
        receipt4 = Receipt.create()
                .withOrderId(UniqueId.createFromString("4"))
                .withProduct(product)
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT2)
                .withCurrentTradedSize(9)
                .withTotalTradedSize(9)
                .withPrice(87)
                .withResidualSize(0)
                ;
        receipt5 = Receipt.create()
                .withOrderId(UniqueId.createFromString("5"))
                .withProduct(product)
                .withExternalAccount("1")
                .withInternalAccount(INTERNAL_ACCOUNT1)
                .withCurrentTradedSize(-2)
                .withTotalTradedSize(-2)
                .withPrice(48)
                .withResidualSize(0)
                ;



        processReceipts();
        positionSnapshot = new PositionSnapshot();
        positionSnapshot.add(new ProductAccountPosition(product.getId(), INTERNAL_ACCOUNT1, 10));
        profitSnapshot = new ProfitSnapshot();
        profitSnapshot.add(new ProductAccountProfit(product.getId(), INTERNAL_ACCOUNT1, -5));
    }

    private void processReceipts()
    {
        storage.onReceipt(receipt1);
        storage.onReceipt(receipt2);
        storage.onReceipt(receipt3);
        storage.onReceipt(receipt4);
        storage.onReceipt(receipt5);
    }

    PositionSnapshot positionSnapshot;
    ProfitSnapshot profitSnapshot;
    ReceiptStorage storage;
    Product product;
    Receipt receipt1;
    Receipt receipt2;
    Receipt receipt3;
    Receipt receipt4;
    Receipt receipt5;
} // class
