package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;



public class ReceiptStorageTest {

    private static String INTERNAL_ACCOUNT1 = "intAccount1";
    private static String INTERNAL_ACCOUNT2 = "intAccount2";

//    @Test
//    public void canProcessReceipts()
//    {
//        assert (storage.getNumberOfReceipts() == 4);
//        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1) == 3);
//        assert (storage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT2) == 1);
//    }
//
//    @Test
//    public void canCalculateCurrentProductPositionOverAllInternalAccounts()
//    {
//        int productPositionGlobal = (int)storage.getPositionForProduct(product.getProductId());
//        assert (productPositionGlobal == +1 +1 +1 +9 -2 +10);
//    }
//
//    @Test
//    public void canCalculateProductPositionForInternalAccount()
//    {
//        int productPositionAccount1 = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT1, product.getProductId());
//        assert (productPositionAccount1 == +1 +1 +1 -2);
//        int productPositionAccount2 = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT2, product.getProductId());
//        assert (productPositionAccount2 == 9);
//    }
//
//    @Test
//    public void canSerializeAndParseItAgain() // CSV is only an example JSON, XML or XLS are fine too.
//    {
//        String csv = storage.toCSV();  //I still don't get this one too.. for me it should be storage.toCSV(),or,storage.toCSV(fileCSV.CSV) or something like that.  
//        ReceiptStorage newStorage = ReceiptStorage.createFromCSV(csv);
//        String newCSV = newStorage.toCSV();
//        assert(csv.compareTo(newCSV) == 0);
//        assert(newStorage.getNumberOfReceipts()==4);
//        assert(newStorage.getNumberOfReceiptsForInternalAccount(INTERNAL_ACCOUNT1)==3);
//    }
//
//    @Test
//    public void canCalculateProductPositionForInternalAccountWithSnapshot()
//    {
//        storage.setPositionSnapshot(positionSnapshot);
//        int productPositionWithSnapshot = (int)storage.getInternalAccountPositionForProduct(INTERNAL_ACCOUNT1, product.getProductId());
//        assert (productPositionWithSnapshot == +1 +1 +1 -2 +10);
//    }
//
//    @Test
//    public void canCalculateProductProfitForInternalAccountWithSnapshot()
//    {
//        int profitWithSnapshot = (int)storage.getInternalAccountProfitForProduct(INTERNAL_ACCOUNT1, product.getProductId());
//        assert (profitWithSnapshot == -2 -2 -2 -5);
//
//    }

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
                ;



        processReceipts();
        positionSnapshot = new PositionSnapshot();
        positionSnapshot.add(new ProductAccountPosition(product.getProductId(), INTERNAL_ACCOUNT1, Decimal.fromDouble(10)));
        profitSnapshot = new ProfitSnapshot();
        profitSnapshot.add(new ProductAccountProfit(product.getProductId(), INTERNAL_ACCOUNT1, Decimal.fromDouble(-5)));
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
