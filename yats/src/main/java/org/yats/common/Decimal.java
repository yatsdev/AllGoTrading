package org.yats.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public static Decimal min(Decimal one, Decimal another) {
        return one.isLessThan(another) ? one : another;
    }

    public static Decimal max(Decimal one, Decimal another) {
        return one.isGreaterThan(another) ? one : another;
    }

    public static Decimal fromDouble(double d) {
        return new Decimal(Double.toString(d));
    }

    public static Decimal fromString(String s) {
        return new Decimal(s);
    }

    public Decimal(String valueString) {
        value = new BigDecimal(valueString);
    }

    public Decimal(BigDecimal b) {
        value = b;
    }

    BigDecimal value;


    public Decimal round() {
        return new Decimal(value.setScale(0, RoundingMode.HALF_UP));
    }

    public Decimal roundToTickSize(Decimal tickSize) {
        return divide(tickSize).round().multiply(tickSize);
    }


}
