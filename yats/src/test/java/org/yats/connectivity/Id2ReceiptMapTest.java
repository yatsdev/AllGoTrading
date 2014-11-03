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
    public void whenExternalIdsAreInMap_sizeExternalIds_countsThem() {
        assert(map.sizeExternalIds()==1);
    }

    @Test
    public void whenOrderIdsAreInMap_sizeOrderIds_countsThem() {
        assert(map.sizeOrderIds()==2);
    }

    @Test
    public void whenOrderIdIsInMap_containsReceiptForOrderId_verifiesThis() {
        assert(!map.containsReceiptForOrderId(orderId1 + "thisMakesIdInvalid"));
        assert(map.containsReceiptForOrderId(orderId1));
    }

    @Test
    public void whenOrderIdIsInMap_getExternalId_returnsTheAssociatedExternalId() {
        assert(map.getExternalId(orderId1).compareTo(externalId1)==0);
    }

    @Test
    public void whenExternalIdIsInMap_containsReceiptForExternalId_verifiesThis() {
        assert(!map.containsReceiptForExternalId(externalId1+"thisMakesIdInvalid"));
        assert(map.containsReceiptForExternalId(externalId1));
    }

    @Test
    public void whenOrderIdIsInMap_removeByOrderId_canRemoveIt() {
        map.remove(orderId1);
        assert(map.sizeOrderIds()==1);
        assert(!map.containsReceiptForExternalId(externalId1));
        assert(!map.containsReceiptForOrderId(orderId1));
    }

    @Test
    public void whenExternalIdIsInMap_toStringJSon_canSerializeItToANotEmptyString() {
        assert(map.toStringJSon().length()>0);
    }

    @Test
    public void whenExternalIdMapGotSerializedToJson_createFromStringJson_canDeserializeIt() {
        String data = map.toStringJSon();
        Id2ReceiptMap newMap = Id2ReceiptMap.createFromStringJson(data);
        String newCsv = newMap.getReceiptForExternalId(externalId1).toStringCSV();
        String oldCsv = receipt1.toStringCSV();
        assert(newCsv.compareTo(oldCsv)==0);
    }

    //////////////////////////////////////////////////////////////
    @BeforeMethod
    public void setUp() {
        map = new Id2ReceiptMap();
        map.putOrderId2ExternalIdMapping(orderId1,externalId1);
        map.putReceipt(externalId1, receipt1);
        map.putOrderId2ExternalIdMapping(orderId2,externalId2);
    }

    Id2ReceiptMap map;
} // class
