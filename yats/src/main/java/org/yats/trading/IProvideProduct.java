package org.yats.trading;

public interface IProvideProduct {
    public Product getProductForProductId(String productId);
    public IProvideProduct getProductsWithUnit(String productId);
}
