package org.yats.trading;

public interface IProvidePosition {
    public java.math.BigDecimal getInternalAccountPositionForProduct(String internalAccount, Product product);
}
