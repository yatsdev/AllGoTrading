package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

import java.util.List;


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
        List<AccountPosition> positions = positionSnapshot.getAllPositionsForOneAccount(account1);
        assert (positions.size()==3);
        Decimal sum=Decimal.ZERO;
        for(AccountPosition p : positions) {
            sum=sum.add(p.getSize());
        }
        assert(sum.isEqualTo(Decimal.fromDouble(6)));
    }

    @Test
    public void canCalculateValuation()
    {
        Position positionInEUR = positionSnapshot.calculateValue(converter, TestMarketData.EUR_PID);

        assert(positionInEUR.getSize().isEqualTo(Decimal.fromString("6.2034298883348440")));
    }


    @BeforeMethod
    public void setUp() {
        PositionSnapshot positionSnapshotTemp = new PositionSnapshot();
        position1 = new AccountPosition(productId1, account1, Decimal.fromDouble(1));
        position2 = new AccountPosition(productId2, account1, Decimal.fromDouble(2));
        position3 = new AccountPosition(productId3, account1, Decimal.fromDouble(3));
        positionSnapshotTemp.add(position1);
        positionSnapshotTemp.add(position2);
        positionSnapshotTemp.add(position3);
        positionSnapshot = PositionSnapshot.fromStringCSV(positionSnapshotTemp.toStringCSV());
        PositionSnapshot positionSnapshot2Temp = new PositionSnapshot();
        position4 = new AccountPosition(productId3, account2, Decimal.fromDouble(3));
        position5 = new AccountPosition(productId3, account2, Decimal.fromDouble(3));
        positionSnapshot2Temp.add(position4);
        positionSnapshot2Temp.add(position5);
        positionSnapshot2 = PositionSnapshot.fromStringCSV(positionSnapshot2Temp.toStringCSV());
        productList = new ProductList();
        productList.read(ProductListTest.PRODUCT_LIST_PATH);
        converter = new RateConverter(productList);
        productList.add(ProductTest.PRODUCT1);
        productList.add(ProductTest.PRODUCT2);
        productList.add(ProductTest.PRODUCT3);
        converter.onMarketData(TestMarketData.EURUSD);
        converter.onMarketData(TestMarketData.GBPUSD);
        converter.onMarketData(TestMarketData.PRODUCT1_DATA);
        converter.onMarketData(TestMarketData.PRODUCT2_DATA);
        converter.onMarketData(TestMarketData.PRODUCT3_DATA);
    }

    String productId1 = ProductTest.PRODUCT1.getProductId();
    String productId2 = ProductTest.PRODUCT2.getProductId();
    String productId3 = ProductTest.PRODUCT3.getProductId();
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
