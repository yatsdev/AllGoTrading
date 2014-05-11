package org.yats.common;

import java.math.BigDecimal;

public class Decimal  {


    public static final Decimal ZERO = new Decimal(BigDecimal.ZERO);
    public static final Decimal ONE = new Decimal(BigDecimal.ONE);


    public BigDecimal toBigDecimal() {
        return value;
    }
    public int toInt() {
        return value.intValue();
    }
    public double toDouble() { return value.doubleValue(); }

    @Override
    public String toString() {
        return value.toString();
    }

    public boolean isLessThan(Decimal d) {
        return value.compareTo(d.toBigDecimal()) < 0;
    }

    public boolean isGreaterThan(Decimal d) {
        return value.compareTo(d.toBigDecimal()) > 0;
    }

    public boolean isEqualTo(Decimal d) {
        return value.compareTo(d.toBigDecimal()) == 0;
    }

    public Decimal add(Decimal other) {
        return new Decimal(value.add(other.value));
    }

    public Decimal subtract(Decimal other) {
        return new Decimal(value.subtract(other.value));
    }

    public Decimal multiply(Decimal other) {
        return new Decimal(value.multiply(other.value));
    }

    public Decimal divide(Decimal other) {
        return new Decimal(value.divide(other.value));
    }

    public Decimal abs() {
        return new Decimal(value.abs());
    }

    public Decimal min(Decimal other) {
        return isLessThan(other) ? this : other;
    }

    public Decimal max(Decimal other) {
        return isGreaterThan(other) ? this : other;
    }

    public static Decimal createFromDouble(double d) {
        return new Decimal(Double.toString(d));
    }

    public Decimal(String valueString) {
        value = new BigDecimal(valueString);
    }

    public Decimal(BigDecimal b) {
        value = b;
    }

    BigDecimal value;


}
