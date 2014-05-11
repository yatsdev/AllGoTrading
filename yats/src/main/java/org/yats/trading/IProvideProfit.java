package org.yats.trading;

import org.yats.common.Decimal;

public interface IProvideProfit {
    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId);
}
