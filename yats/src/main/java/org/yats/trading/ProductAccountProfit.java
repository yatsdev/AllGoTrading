package org.yats.trading;

import java.math.BigDecimal;

public class ProductAccountProfit {

    public String getProductId() {
        return productId;
    }


    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId+","+internalAccount;
    }

    public ProductAccountProfit(String productId, String internalAccount, BigDecimal size) {
        this.productId = productId;
        this.internalAccount = internalAccount;
        this.profit = size;
    }

    public ProductAccountProfit add(ProductAccountProfit other) {
        return new ProductAccountProfit(productId, internalAccount, profit.add(other.profit));
    }

    String productId;
    String internalAccount;
    BigDecimal profit;
} // class
