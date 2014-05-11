package org.yats.trading;

import org.yats.common.Decimal;

public interface IProvidePosition {
    public Decimal getInternalAccountPositionForProduct(String internalAccount, String productId);
}
