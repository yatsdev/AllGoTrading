package org.yats.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Decimal implements Comparable<Decimal> {


    public static final Decimal MINUSONE = new Decimal("-1");
    public static final Decimal ZERO = new Decimal(BigDecimal.ZERO);
    public static final Decimal ONE = new Decimal(BigDecimal.ONE);
    public static final Decimal TWO = new Decimal("2");
    public static final Decimal CENT = new Decimal("0.01");

    public Decimal negate() {
        return this.multiply(Decimal.MINUSONE);
    }

    private static final int DEFAULT_SCALE = 10;
    public static final Decimal TEN = new Decimal("10");
//    public static final Decimal HUNDRED = new Decimal("100");


    @Override
    public int compareTo(Decimal o) {
        if(o.isEqualTo(this)) return 0;
        return this.isGreaterThan(o) ? 1 : -1;
    }

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

    public String toString2Digits(int digits) {
        BigDecimal d = new BigDecimal(value.toString());
        return d.setScale(digits, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    public Decimal round() {
        return new Decimal(value.setScale(0, RoundingMode.HALF_UP));
    }

    public Decimal roundToTickSize(Decimal tickSize) {
        return divide(tickSize).round().multiply(tickSize);
    }
    public Decimal roundToDigits(int digits) {
        int multiplier = (int) Math.round(Math.pow(10,digits));
        return multiply(Decimal.fromDouble(multiplier)).round().divide(Decimal.fromDouble(multiplier));
    }


    public Decimal invert() {
        return Decimal.ONE.divide(this);
    }

    public boolean isZero() {
        return equals(Decimal.ZERO);
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
        return new Decimal(value.divide(other.value, DEFAULT_SCALE, RoundingMode.HALF_UP));
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
        if(s.contains(".")) {
            return new Decimal(s.replace(",",""));
        }
        return new Decimal(s.replace(",","."));
    }

    public Decimal(String valueString) {
        value = new BigDecimal(valueString);

    }

    public Decimal(BigDecimal b) {
        value = b;
    }

    BigDecimal value;



}
