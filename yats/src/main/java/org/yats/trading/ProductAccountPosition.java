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

    private String internalAccount;
    private String productId;
    private Decimal size;

    public void setInternalAccount(String internalAccount) {
        this.internalAccount = internalAccount;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setSize(Decimal size) {
        this.size = size;
    }

} // class
