package org.yats.trading;

import org.yats.common.Decimal;

public class BookRow {

    @Override
    public String toString() {
        return "BookRow{" +
                "size=" + size +
                ", price=" + price +
                '}';
    }

    public boolean isSameAs(BookRow _row) {
        if(!isPrice(_row.price)) return false;
        if(!isSize(_row.size)) return false;
        return true;
    }

    public boolean isPrice(Decimal _price) {
        return price.isEqualTo(_price);
    }

    public boolean isSize(Decimal _size) {
        return size.isEqualTo(_size);
    }

    public String toStringCSV() {
        if(size.isEqualTo(Decimal.ZERO)) return "";
        return size + "," + price;
    }

    public static BookRow fromStringCSV(String csv) {
        String[] parts = csv.split(",");
        if(parts.length<2) //throw new CommonExceptions.FieldNotFoundException("too few fields!");
            return new BookRow(new Decimal("0"), new Decimal("-1"));
        return new BookRow(new Decimal(parts[0]), new Decimal(parts[1]));
    }

    public Decimal getSize() {
        return size;
    }

    public void setSize(Decimal size) {
        this.size = size;
    }

    public Decimal getPrice() {
        return price;
    }

    public void setPrice(Decimal price) {
        this.price = price;
    }

    public BookRow(Decimal _size, Decimal _price) {
        this.size = _size;
        this.price = _price;
    }

    public BookRow(String _size, String _price) {
        this.size = Decimal.fromString(_size);
        this.price = Decimal.fromString(_price);
    }

    private Decimal size;
    private Decimal price;


} // class
