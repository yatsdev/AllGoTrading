package org.yats.trading;

import org.yats.common.Decimal;

public class Position {

    @Override
    public String toString() {
        return "Position{" +
                "productId='" + productId + '\'' +
                ", size=" + size.roundToDigits(5) +
                '}';
    }

    public Position add(Receipt receipt) {
        Decimal newSize = size.add(receipt.getCurrentTradedSizeSigned());
        return new Position(productId, newSize);
    }

    public Position add(Position other) {
        if(!other.isForProductId(productId)) return this;
        return new Position(productId, size.add(other.getSize()));
    }

    public boolean isSize(int _size) {
        return (size.toInt() == _size);
    }

    public boolean isSize(Decimal _size) {
        return (size.isEqualTo(_size));
    }

    public boolean isSize(Decimal _size, int digits) {
        return size.roundToDigits(digits).isEqualTo(_size.roundToDigits(digits));
    }

    public boolean isSameAs(Position other) {
        if(!isForProductId(other.productId)) return false;
        return size.isEqualTo(other.size);
    }

    public boolean isForProductId(String otherProductId) {
        return (this.productId.compareTo(otherProductId)==0);
    }

    public String toStringCSV() {
        return productId + "," + size.toString();
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

    public Position(String productId, Decimal size) {
        this.productId = productId;
        this.size = size;
    }

    protected String productId;
    protected Decimal size;


    public Position subtract(Position oldPosition) {
        if(!oldPosition.isForProductId(productId)) throw new TradingExceptions.UnknownIdException(""+productId+"!="+oldPosition.getProductId());
        return new Position(productId, size.subtract(oldPosition.getSize()));
    }
} // class
