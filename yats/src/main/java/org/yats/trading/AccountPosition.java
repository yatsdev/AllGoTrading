package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;

public class AccountPosition extends Position {

    public String toStringCSV() {
        return new StringBuilder()
                .append(getProductId()).append(",")
                .append(internalAccount).append(",")
                .append(getSize().toString())
                .toString();
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public AccountPosition add(AccountPosition other) {
        return new AccountPosition(productId, internalAccount, size.add(other.size));
    }

    public AccountPosition add(Receipt receipt) {
        Decimal newSize = size.add(receipt.getCurrentTradedSizeSigned());
        return new AccountPosition(productId, internalAccount, newSize);
    }

    public AccountPosition(String productId, String internalAccount, Decimal size) {
        super(productId, size);
        this.internalAccount = internalAccount;
    }

    public boolean isSameAs(AccountPosition other) {
        if(internalAccount.compareTo(other.internalAccount)!=0) return false;
        return super.isSameAs(other);
    }

    private String internalAccount;

    public static AccountPosition fromStringCSV(String csv) {
        String[] fieldStrings = csv.split(",");
        if(fieldStrings.length!=3) CommonExceptions.throwFieldNotFoundException("found length=" + fieldStrings.length);
        return new AccountPosition(fieldStrings[0],fieldStrings[1],new Decimal(fieldStrings[2]));
    }

} // class
