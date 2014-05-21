package org.yats.trading;

import org.yats.common.Decimal;

public class Position {

    public String getProductId() {
        return productId;
    }

    public Decimal getSize() {
        return size;
    }

    public Position(String productId, Decimal size) {
        this.productId = productId;
        this.size = size;
    }


    public Position add(Receipt receipt) {
        Decimal newSize = size.add(receipt.getCurrentTradedSizeSigned());
        return new Position(productId, newSize);
    }

    String productId;
    Decimal size;

} // class
