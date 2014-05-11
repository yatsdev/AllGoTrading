package org.yats.trading;

public interface IProvideProfit {
    public java.math.BigDecimal getInternalAccountProfitForProduct(String internalAccount, String productId);
}
