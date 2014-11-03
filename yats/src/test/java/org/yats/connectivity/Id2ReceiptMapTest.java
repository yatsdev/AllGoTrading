package org.yats.connectivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.*;

@Test(groups = { "inMemory" })
public class Id2ReceiptMapTest {

    private static String orderId1 = "orderId1";
    private static String externalId1 = "externalId1";
    private static String orderId2 = "orderId2";
    private static String externalId2 = "externalId2";

    private static String orderIdBID = "orderIdBID";
    private static String externalIdBID = "externalIdBID";
    private static String orderIdBIDCancel = "orderIdBIDCancel";

    private static String orderIdASK = "orderIdASK";
    private static String externalIdASK = "externalIdASK";


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

            static OrderNew orderBID = OrderNew.create()
            .withBookSide(BookSide.BID)
            .withProductId("OANDA_NZDJPY")
            .withOrderId(UniqueId.create())
            .withTimestamp(DateTime.now(DateTimeZone.UTC));

            static OrderCancel orderCancelBID = orderBID.createCancelOrder();

            static Receipt defaultReceiptBID = orderBID.createReceiptDefault().withEndState(false);

            static OrderNew orderASK = OrderNew.create()
            .withBookSide(BookSide.ASK)
            .withProductId("OANDA_NZDJPY")
            .withOrderId(UniqueId.create())
            .withTimestamp(DateTime.now(DateTimeZone.UTC));

            static Receipt defaultReceiptASK = orderASK.createReceiptDefault().withEndState(false);



    @Test
    public void whenExternalIdsAreInMap_sizeExternalIds_countsThem() {
        assert(map.sizeExternalIds()==3);
    }

    @Test
    public void whenOrderIdsAreInMap_sizeOrderIds_countsThem() {
        assert(map.sizeOrderIds()==4);
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
        assert(map.sizeOrderIds()==3);
        assert(!map.containsReceiptForExternalId(externalId1));
        assert(!map.containsReceiptForOrderId(orderId1));
    }

    @Test
    public void whenExternalIdIsInMap_toStringCSVExternalId2OrderMap_canSerializeIt() {
        assert(map.toStringCSVExternalId2OrderMap().length()>0);
    }

    @Test
    public void whenExternalIdMapGotSerialized_parseExternalId2OrderMap_canDeserializeIt() {
        String data = map.toStringCSVExternalId2OrderMap();
        Id2ReceiptMap newMap = new Id2ReceiptMap("test");
        newMap.parseExternalId2OrderMap(data);
        String newCsv = newMap.getReceiptForExternalId(externalId1).toStringCSV();
        String oldCsv = receipt1.toStringCSV();
        assert(newCsv.compareTo(oldCsv)==0);
    }

    @Test
    public void whenExternalIdMapGotSerialized_parseExternalId2OrderMap_canDeserializeBID() {
        String data = map.toStringCSVExternalId2OrderMap();
        Id2ReceiptMap newMap = new Id2ReceiptMap("testBID");
        newMap.parseExternalId2OrderMap(data);
        String newCsv = newMap.getReceiptForExternalId(externalIdBID).toStringCSV();
        String oldCsv = defaultReceiptBID.toStringCSV();
        assert(newCsv.compareTo(oldCsv)==0);
    }

    @Test
    public void whenExternalIdMapGotSerialized_parseExternalId2OrderMap_canDeserializeASK() {
        String data = map.toStringCSVExternalId2OrderMap();
        Id2ReceiptMap newMap = new Id2ReceiptMap("testASK");
        newMap.parseExternalId2OrderMap(data);
        String newCsv = newMap.getReceiptForExternalId(externalIdASK).toStringCSV();
        String oldCsv = defaultReceiptASK.toStringCSV();
        assert(newCsv.compareTo(oldCsv)==0);
    }



    //////////////////////////////////////////////////////////////
    @BeforeMethod
    public void setUp() {
        map = new Id2ReceiptMap("test");
        map.putOrderId2ExternalIdMapping(orderId1,externalId1);
        map.putReceipt(externalId1, receipt1);
        map.putOrderId2ExternalIdMapping(orderId2,externalId2);

        map.putOrderId2ExternalIdMapping(orderIdBIDCancel,externalIdBID);
        map.putReceipt(externalIdBID, defaultReceiptBID);

        map.putOrderId2ExternalIdMapping(orderIdASK,externalIdASK);
        map.putReceipt(externalIdASK, defaultReceiptASK);
    }

    Id2ReceiptMap map;
} // class
