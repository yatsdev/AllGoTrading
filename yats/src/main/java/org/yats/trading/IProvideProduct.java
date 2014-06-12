package org.yats.trading;

import java.util.Collection;

public interface IProvideProduct {
    public Product getProductForProductId(String productId);
    public IProvideProduct getProductsWithUnit(String productId);
    public IProvideProduct getProductsWithUnderlying(String productId);
    public Collection<Product> values();
}
