package org.yats.trading;

public class PositionRequest {

    public String getAccount() {
        return account;
    }

    public String getProductId() {
        return productId;
    }

    public PositionRequest(String account, String productId) {
        this.account = account;
        this.productId = productId;
    }

    private String account;
    private String productId;
}
