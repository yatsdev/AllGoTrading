package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

import java.util.ArrayList;

public class PositionServerTest {

    @Test
    public void canProcessReceipts() {
        assert (positionServer.getNumberOfReceipts() == 5);
    }

    @Test
    public void canCalculateCurrentPositionOverAllInternalAccounts() {
        Position p = positionServer.getPositionForAllAccounts(ProductTest.TEST_PRODUCT1_ID);
        assert (p.isSize(+1 +1 +1 +9 -2));
    }

    @Test
    public void canCalculatePositionForInternalAccount()
    {
        Position positionAccount1 = positionServer.getAccountPosition(new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT1, ProductTest.TEST_PRODUCT1_ID));
        assert (positionAccount1.isSize(+1 +1 +1 -2));
        Position positionAccount2 = positionServer.getAccountPosition(new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT2, ProductTest.TEST_PRODUCT1_ID));
        assert (positionAccount2.isSize(9));
    }

    @Test
    public void canCalculatePositionForInternalAccountWithSnapshot() {
        positionServer.addPositionSnapshot(positionSnapshot);
        Position positionWithSnapshot = positionServer.getAccountPosition(positionRequest1);
        assert(positionWithSnapshot.isSize(+1 + 1 + 1 -2 +10));
    }

    @Test
    public void canCalculateCounterPositionForLeveragedProduct() {

        PositionServer positionServer = new PositionServer();
        positionServer.setProductList(productList);
        positionServer.onReceipt(ReceiptTest.RECEIPT6);
        String pid = ReceiptTest.RECEIPT6.getProductId();
        AccountPosition pos = positionServer.getAccountPosition(
                new PositionRequest(ReceiptTest.RECEIPT6.getInternalAccount(), pid));
        Decimal currentTradedSizeSigned = ReceiptTest.RECEIPT6.getCurrentTradedSizeSigned();
        assert(currentTradedSizeSigned.isEqualTo(pos.getSize()));

        Product p = productList.getProductWith(pid);
        String pidUnit = productList.getUnitOfProductWithId(pid).getProductId();
        Decimal price = ReceiptTest.RECEIPT6.getPrice();
        AccountPosition posCounter = positionServer.getAccountPosition(
                new PositionRequest(ReceiptTest.RECEIPT6.getInternalAccount(), pidUnit));
        Decimal expectedCounterSize = currentTradedSizeSigned.negate().multiply(p.getContractSize()).multiply(price);
        assert(expectedCounterSize.isEqualTo(posCounter.getSize()));
    }

    @Test
    public void buyLowSellHighGivesProfit() {
        PositionServer positionServer = new PositionServer();
        positionServer.setProductList(productList);
        positionServer.onReceipt(ReceiptTest.RECEIPT7);
        positionServer.onReceipt(ReceiptTest.RECEIPT6);

        String pid = ReceiptTest.RECEIPT6.getProductId();
        String pidUnit = productList.getUnitOfProductWithId(pid).getProductId();
        AccountPosition posCounter = positionServer.getAccountPosition(
                new PositionRequest(ReceiptTest.RECEIPT6.getInternalAccount(), pidUnit));
        Decimal expectedCounterSize = Decimal.fromDouble(3*2*10);
        assert(expectedCounterSize.isEqualTo(posCounter.getSize()));
    }

    @Test
    public void sellLowBuyHighGivesLoss() {
        PositionServer positionServer = new PositionServer();
        positionServer.setProductList(productList);
        positionServer.onReceipt(ReceiptTest.RECEIPT6);
        positionServer.onReceipt(ReceiptTest.RECEIPT8);
        String pid = ReceiptTest.RECEIPT6.getProductId();
        String pidUnit = productList.getUnitOfProductWithId(pid).getProductId();
        AccountPosition posCounter = positionServer.getAccountPosition(
                new PositionRequest(ReceiptTest.RECEIPT6.getInternalAccount(), pidUnit));
        Decimal expectedCounterSize = Decimal.fromDouble(-2 * 2 * 10);
        assert(expectedCounterSize.isEqualTo(posCounter.getSize()));
    }


    @Test
    public void canStorePositionSnapshot() {
        positionServer.onReceipt(ReceiptTest.RECEIPT1);
        assert(1==positionStorage.getSnapshotCount());
    }

    @Test
    public void canInitialiseFromPositionSnapshot() {
        PositionServer originalServer = new PositionServer();
        originalServer.setPositionStorage(positionStorage);
        originalServer.setProductList(productList);
        originalServer.onReceipt(ReceiptTest.RECEIPT1);
        originalServer.onReceipt(ReceiptTest.RECEIPT2);
        assert(2==positionStorage.getSnapshotCount());
        PositionRequest pr = new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT1, ProductTest.TEST_PRODUCT1_ID);
        PositionServer newPositionServer = new PositionServer();
        newPositionServer.setPositionStorage(positionStorage);
        assert(newPositionServer.getAccountPosition(pr).isSize(0));
        newPositionServer.initFromLastStoredPositionSnapshot();
        assert(newPositionServer.getAccountPosition(pr).isSize(2));
    }


    @BeforeMethod
    public void setUp() {
        positionServer = new PositionServer();
        productList = ProductList.createFromFile(ProductListTest.PRODUCT_LIST_PATH);
        positionServer.setProductList(productList);
        processReceipts();
        positionSnapshot = new PositionSnapshot();
        AccountPosition p;
        p = new AccountPosition(ProductTest.TEST_PRODUCT1_ID, ReceiptTest.INTERNAL_ACCOUNT1, Decimal.fromDouble(10));
        positionSnapshot.add(p);
        positionRequest1 = new PositionRequest(ReceiptTest.INTERNAL_ACCOUNT1, ProductTest.TEST_PRODUCT1_ID);
        positionStorage = new PositionSnapshotStorageMem();
        positionServer.setPositionStorage(positionStorage);
    }

    private void processReceipts() {
        positionServer.onReceipt(ReceiptTest.RECEIPT1);
        positionServer.onReceipt(ReceiptTest.RECEIPT2);
        positionServer.onReceipt(ReceiptTest.RECEIPT3);
        positionServer.onReceipt(ReceiptTest.RECEIPT4);
        positionServer.onReceipt(ReceiptTest.RECEIPT5);
    }


    private ProductList productList;
    private PositionServer positionServer;
    private PositionSnapshotStorageMem positionStorage;

//    private static String INTERNAL_ACCOUNT1 = "intAccount1";
//    private static String INTERNAL_ACCOUNT2 = "intAccount2";
    PositionSnapshot positionSnapshot;
    PositionRequest positionRequest1;


    private class PositionSnapshotStorageMem implements IStorePositionSnapshots  {
        @Override
        public void store(PositionSnapshot positionSnapshot) {
            positionSnapshots.add(positionSnapshot);

        }
        @Override
        public PositionSnapshot readLast() {
            return positionSnapshots.get(positionSnapshots.size()-1);
        }
        public int getSnapshotCount() {return positionSnapshots.size();}

        private PositionSnapshotStorageMem() {
            positionSnapshots=new ArrayList<PositionSnapshot>();
        }
        ArrayList<PositionSnapshot> positionSnapshots;
    }

}
