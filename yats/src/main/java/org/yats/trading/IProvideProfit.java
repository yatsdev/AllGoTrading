package org.yats.trading;
import java.math.BigDecimal;

public interface IProvideProfit {
    public BigDecimal getInternalAccountProfitForProduct(String internalAccount, String productId);
}
