package org.yats.trading;

public class ProductAccountPosition {

    public String getProductId() {
        return productId;
    }

    public java.math.BigDecimal getSize() {
        return size;
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public ProductAccountPosition(String productId, String internalAccount, java.math.BigDecimal size) {
        this.productId = productId;
        this.internalAccount = internalAccount;
        this.size = size;
    }

    public ProductAccountPosition add(ProductAccountPosition other) {
        return new ProductAccountPosition(productId, internalAccount, size.add(other.size));
    }

    String internalAccount;
    String productId;
    java.math.BigDecimal size;

} // class
