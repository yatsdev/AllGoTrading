package org.yats.trading;

public interface IProvidePosition {
    public double getInternalAccountPositionForProduct(String internalAccount, Product product);
}
