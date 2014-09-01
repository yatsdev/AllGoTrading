package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;


public class PositionSnapshotTest {

    @Test
    public void canCalculateAccountPosition()
    {
        assert (isPositionCorrect(account1, productId1, 1));
        assert (isPositionCorrect(account1, productId2, 2));
        assert (isPositionCorrect(account1, productId3, 3));
    }

    @Test
    public void canCalculateAccountPositionAfterAddingOtherSnapshot()
    {
        positionSnapshot.add(positionSnapshot2);
        assert(isPositionCorrect(account2, productId3, 6));
    }

    private boolean isPositionCorrect(String account, String productId, int size) {
        PositionRequest request = new PositionRequest(account, productId);
        AccountPosition p1 = positionSnapshot.getAccountPosition(request);
        return p1.isSize(size);
    }

      @Test
    public void canCalculatePositionForAllAccountsAfterAddingOtherSnapshot()
    {
        positionSnapshot.add(positionSnapshot2);
        Position p1 =  positionSnapshot.getPositionForAllAccounts(productId1);
        assert (p1.isSize(1));

        Position p2 =  positionSnapshot.getPositionForAllAccounts(productId3);
        assert (p2.isSize(9));
    }

    @Test
    public void canCalculateAllPositionsForOneAccount()
    {
        IProvidePosition positions = positionSnapshot.getAllPositionsForOneAccount(account1);
        assert (positions.getAllPositions().size()==3);
        Decimal sum=Decimal.ZERO;
        for(AccountPosition p : positions.getAllPositions()) {
            sum=sum.add(p.getSize());
        }
        assert(sum.isEqualTo(Decimal.fromDouble(6)));
    }

    @Test
    public void canCalculateValuationForAccountProduct()
    {
        PositionRequest r = new PositionRequest(account1, productId1);
        Position positionInUSD = positionSnapshot.getValueForAccountProduct(TestPriceData.TEST_USD_PID, r);
        Decimal expected =
                Decimal.ONE
                .multiply(TestPriceData.PRODUCT1_DATA.getLast())
                .multiply(TestPriceData.TEST_EURUSD.getLast());
        assert(positionInUSD.getSize().isEqualTo(expected));
    }

    @Test
    public void canCalculateValuationForAllPositions()
    {
        Position positionInEUR = positionSnapshot.getValueForAllPositions(TestPriceData.TEST_EUR_PID);
        assert(positionInEUR.getSize().isEqualTo(Decimal.fromString("6.2034298883348440")));
    }

    @Test
    public void canCalculateProfitFromDifferentSnapshots()
    {
        Position positionInUSD = positionSnapshot.getValueForAllPositions(TestPriceData.TEST_USD_PID);
        Position positionInEUR = positionSnapshot.getValueForAllPositions(TestPriceData.TEST_EUR_PID);
        Position profitInEUR = converter.calculateProfit(positionInUSD, positionInEUR, TestPriceData.TEST_EUR_PID);
        assert(profitInEUR.isSize(Decimal.ZERO,5));
    }

    @Test
    public void verifyThatProfitFromSameSnapshotIsZero()
    {
        Position positionInUSD = positionSnapshot.getValueForAllPositions(TestPriceData.TEST_EUR_PID);
        Position positionInEUR = positionSnapshot.getValueForAllPositions(TestPriceData.TEST_EUR_PID);
        Position profitInEUR = converter.calculateProfit(positionInUSD, positionInEUR, TestPriceData.TEST_EUR_PID);
        assert(profitInEUR.isSize(Decimal.ZERO,5));
    }


    @BeforeMethod
    public void setUp() {
        productList = new ProductList();
        productList.read(ProductListTest.PRODUCT_LIST_PATH);
//        productList.add(ProductTest.PRODUCT1);
//        productList.add(ProductTest.PRODUCT2);
//        productList.add(ProductTest.PRODUCT3);
        converter = new RateConverter(productList);
        converter.onPriceData(TestPriceData.TEST_EURUSD);
        converter.onPriceData(TestPriceData.TEST_GBPUSD);
        converter.onPriceData(TestPriceData.PRODUCT1_DATA);
        converter.onPriceData(TestPriceData.PRODUCT2_DATA);
        converter.onPriceData(TestPriceData.PRODUCT3_DATA);

        PositionSnapshot positionSnapshotTemp = new PositionSnapshot();
        position1 = new AccountPosition(productId1, account1, Decimal.fromDouble(1));
        position2 = new AccountPosition(productId2, account1, Decimal.fromDouble(2));
        position3 = new AccountPosition(productId3, account1, Decimal.fromDouble(3));
        positionSnapshotTemp.add(position1);
        positionSnapshotTemp.add(position2);
        positionSnapshotTemp.add(position3);
        positionSnapshot = PositionSnapshot.fromStringCSV(positionSnapshotTemp.toStringCSV());
        positionSnapshot.setRateConverter(converter);
        PositionSnapshot positionSnapshot2Temp = new PositionSnapshot();
        position4 = new AccountPosition(productId3, account2, Decimal.fromDouble(3));
        position5 = new AccountPosition(productId3, account2, Decimal.fromDouble(3));
        positionSnapshot2Temp.add(position4);
        positionSnapshot2Temp.add(position5);
        positionSnapshot2 = PositionSnapshot.fromStringCSV(positionSnapshot2Temp.toStringCSV());
        positionSnapshot2.setRateConverter(converter);
    }

    String productId1 = ProductTest.TEST_PRODUCT1_ID;
    String productId2 = ProductTest.TEST_PRODUCT2_ID;
    String productId3 = ProductTest.TEST_PRODUCT3_ID;
    String account1 = "account1";
    String account2 = "account2";
    PositionSnapshot positionSnapshot;
    PositionSnapshot positionSnapshot2;
    AccountPosition position1;
    AccountPosition position2;
    AccountPosition position3;
    AccountPosition position4;
    AccountPosition position5;
    RateConverter converter;
    ProductList productList;

} // class
