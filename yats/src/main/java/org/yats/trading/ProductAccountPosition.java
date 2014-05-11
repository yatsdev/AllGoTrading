package org.yats.trading;

import org.yats.common.Decimal;

public class ProductAccountPosition {

    public String getProductId() {
        return productId;
    }

    public Decimal getSize() {
        return size;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public ProductAccountPosition(String productId, String internalAccount, Decimal size) {
        this.productId = productId;
        this.internalAccount = internalAccount;
        this.size = size;
    }

    public ProductAccountPosition add(ProductAccountPosition other) {
        return new ProductAccountPosition(productId, internalAccount, size.add(other.size));
    }

    String internalAccount;
    String productId;
    Decimal size;

} // class
