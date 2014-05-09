package org.yats.trading;

public class ProductAccountPosition {

    public String getProductId() {
        return productId;
    }

    public double getSize() {
        return size;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public ProductAccountPosition(String productId, String internalAccount, double size) {
        this.productId = productId;
        this.internalAccount = internalAccount;
        this.size = size;
    }

    public ProductAccountPosition add(ProductAccountPosition other) {
        return new ProductAccountPosition(productId, internalAccount, size+other.size);
    }

    String internalAccount;
    String productId;
    double size;

} // class
