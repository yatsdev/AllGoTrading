package org.yats.connectivity;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.Receipt;
import org.yats.trading.TestPriceData;

@Test(groups = { "inMemory" })
public class Id2ReceiptMapTest {

    private static String orderId1 = "orderId1";
    private static String externalId1 = "externalId1";
    private static String orderId2 = "orderId2";
    private static String externalId2 = "externalId2";

    private static Receipt receipt1 = Receipt.create()
            .withBookSide(BookSide.BID)
            .withCurrentTradedSize(Decimal.ZERO)
            .withEndState(false)
            .withExternalAccount("extAcc")
            .withInternalAccount("intAcc")
            .withPrice(Decimal.TEN)
            .withOrderId(UniqueId.create())
            .withProductId(TestPriceData.TEST_SAP_PID)
            .withRejectReason("")
            .withResidualSize(Decimal.ONE);

    @Test
    public void whenIdsArePutInMap_canCountThem() {
        assert(map.size()==2);
    }

    @Test
    public void whenIdsArePutInMap_canGetThemByKey() {
        assert(map.get(orderId1).compareTo(externalId1)==0);
    }


    @BeforeMethod(groups = { "inMemory" })
    public void setUp() {
        map = new Id2ReceiptMap();
        map.putOrderId2ExternalIdMapping(orderId1,externalId1);
        map.putOrderId2ExternalIdMapping(orderId2,externalId2);
    }

    Id2ReceiptMap map;
} // class
