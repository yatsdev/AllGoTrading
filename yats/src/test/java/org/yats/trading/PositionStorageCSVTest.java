package org.yats.trading;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.FileTool;

public class PositionStorageCSVTest {

    private static String filename = "PositionStorageCSVTest.csv";

    @Test
    public void canWriteAndReadLastCSV()
    {
        FileTool.deleteFile(filename);
        PositionStorageCSV storage = new PositionStorageCSV(filename);
        PositionSnapshot p1 = new PositionSnapshot();
        p1.add(new AccountPosition(ProductTest.PRODUCT1.getProductId(),  ReceiptTest.INTERNAL_ACCOUNT1, new Decimal("11")));
        p1.add(new AccountPosition(ProductTest.PRODUCT2.getProductId(),  ReceiptTest.INTERNAL_ACCOUNT2, new Decimal("22")));
        storage.store(p1);
        PositionSnapshot p2 = new PositionSnapshot();
        p2.add(new AccountPosition(ProductTest.PRODUCT1.getProductId(),  ReceiptTest.INTERNAL_ACCOUNT1, new Decimal("66")));
        p2.add(new AccountPosition(ProductTest.PRODUCT2.getProductId(),  ReceiptTest.INTERNAL_ACCOUNT2, new Decimal("77")));
        storage.store(p2);
        PositionSnapshot newSnapshot = storage.readLast();
        assert (2==newSnapshot.size());
        assert (newSnapshot.isSameAs(p2));
        FileTool.deleteFile(filename);
    }

    @BeforeMethod
    public void setUp() {

    }

}
