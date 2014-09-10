package org.yats.trading;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;

public class ReceiptTest {

    public static String INTERNAL_ACCOUNT1 = "intAccount1";
    public static String INTERNAL_ACCOUNT2 = "intAccount2";

    public final static Receipt RECEIPT1 = Receipt.create()
            .withOrderId(UniqueId.createFromString("1"))
            .withProductId(ProductTest.TEST_PRODUCT1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.ONE)
            .withTotalTradedSize(Decimal.ONE)
            .withPrice(Decimal.fromDouble(50))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.BID)
            ;
    public final static Receipt RECEIPT2 = Receipt.create()
            .withOrderId(UniqueId.createFromString("2"))
            .withProductId(ProductTest.TEST_PRODUCT1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.ONE)
            .withTotalTradedSize(Decimal.ONE)
            .withPrice(Decimal.fromDouble(50))
            .withResidualSize(Decimal.ONE)
            .withBookSide(BookSide.BID)
            ;
    public final static Receipt RECEIPT3 = Receipt.create()
            .withOrderId(UniqueId.createFromString("2"))
            .withProductId(ProductTest.TEST_PRODUCT1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.ONE)
            .withTotalTradedSize(Decimal.fromDouble(2))
            .withPrice(Decimal.fromDouble(50))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.BID)
            ;
    public final static Receipt RECEIPT4 = Receipt.create()
            .withOrderId(UniqueId.createFromString("4"))
            .withProductId(ProductTest.TEST_PRODUCT1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT2)
            .withCurrentTradedSize(Decimal.fromDouble(9))
            .withTotalTradedSize(Decimal.fromDouble(9))
            .withPrice(Decimal.fromDouble(87))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.BID)
            ;
    public final static Receipt RECEIPT5 = Receipt.create()
            .withOrderId(UniqueId.createFromString("5"))
            .withProductId(ProductTest.TEST_PRODUCT1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.fromDouble(2))
            .withTotalTradedSize(Decimal.fromDouble(2))
            .withPrice(Decimal.fromDouble(48))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.ASK)
            ;
    public final static Receipt RECEIPT6 = Receipt.create()
            .withOrderId(UniqueId.createFromString("5"))
            .withProductId(ProductTest.TEST_LEVERAGED1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.fromDouble(2))
            .withTotalTradedSize(Decimal.fromDouble(2))
            .withPrice(Decimal.fromDouble(48))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.ASK)
            ;
    public final static Receipt RECEIPT7 = Receipt.create()
            .withOrderId(UniqueId.createFromString("5"))
            .withProductId(ProductTest.TEST_LEVERAGED1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.fromDouble(2))
            .withTotalTradedSize(Decimal.fromDouble(2))
            .withPrice(Decimal.fromDouble(45))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.BID)
            ;
    public final static Receipt RECEIPT8 = Receipt.create()
            .withOrderId(UniqueId.createFromString("5"))
            .withProductId(ProductTest.TEST_LEVERAGED1_ID)
            .withExternalAccount("1")
            .withInternalAccount(INTERNAL_ACCOUNT1)
            .withCurrentTradedSize(Decimal.fromDouble(2))
            .withTotalTradedSize(Decimal.fromDouble(2))
            .withPrice(Decimal.fromDouble(50))
            .withResidualSize(Decimal.ZERO)
            .withBookSide(BookSide.BID)
            ;
}
