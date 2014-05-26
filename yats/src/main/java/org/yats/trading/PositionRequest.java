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

    public boolean isSameAccountAs(Receipt receipt) {
        return account.compareTo(receipt.getInternalAccount()) == 0;
    }

    public boolean isSameProductIdAs(Receipt receipt) {
        return productId.compareTo(receipt.getProductId()) == 0;
    }

    public boolean isForReceipt(Receipt receipt) {
        return isSameAccountAs(receipt) && isSameProductIdAs(receipt);
    }
}
