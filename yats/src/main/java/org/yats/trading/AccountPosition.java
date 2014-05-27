package org.yats.trading;

import org.yats.common.Decimal;

public class AccountPosition extends Position {


    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public AccountPosition add(AccountPosition other) {
        return new AccountPosition(productId, size.add(other.size), internalAccount);
    }

    public AccountPosition add(Receipt receipt) {
        Decimal newSize = size.add(receipt.getCurrentTradedSizeSigned());
        return new AccountPosition(productId, newSize, internalAccount);
    }

    public AccountPosition(String productId, Decimal size, String internalAccount) {
        super(productId, size);
        this.internalAccount = internalAccount;
    }



    private String internalAccount;

} // class
