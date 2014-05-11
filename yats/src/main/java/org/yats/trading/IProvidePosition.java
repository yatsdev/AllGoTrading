package org.yats.trading;
import java.math.BigDecimal;

public interface IProvidePosition {
    public BigDecimal getInternalAccountPositionForProduct(String internalAccount, String productId);
}
