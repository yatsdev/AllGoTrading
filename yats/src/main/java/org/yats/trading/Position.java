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

    

    public boolean isSize(int _size) {
        return (size.toInt() == _size);
    }

    public boolean isSameAs(Position other) {
        if(productId.compareTo(other.productId)!=0) return false;
        if(!size.isEqualTo(other.size)) return false;
        return true;
    }

    public String toStringCSV() {
        return new StringBuilder().append(productId).append(",").append(size.toString()).toString();
    }
    
    public String getProductId() {
        return productId;
    }

    public Decimal getSize() {
        return size;
    }

    public void setSize(Decimal size) {
        this.size = size;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    protected String productId;
    protected Decimal size;
    
    
} // class
