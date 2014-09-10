package org.yats.trading;

import java.util.Collection;

public interface IProvideProduct {
    public boolean containsProductWith(String productId);
    public Product getProductWith(String productId);
    public IProvideProduct getProductsWithUnit(String productId);
    public IProvideProduct getProductsWithUnderlying(String productId);
    public Collection<Product> values();
}
