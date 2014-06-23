package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DecimalTest {

    @Test
    public void canCompareTwoDecimals() {
        assert(0==Decimal.fromDouble(10).compareTo(Decimal.fromDouble(10)));
        assert(0!=Decimal.fromDouble(10).compareTo(Decimal.fromDouble(11)));
        assert(0 > Decimal.fromDouble(10).compareTo(Decimal.fromDouble(11)));
        assert(0 < Decimal.fromDouble(12).compareTo(Decimal.fromDouble(11)));
    }

    @Test
    public void canCreateFromStringInDifferentFormats() {
        assert(!Decimal.fromString("1,265.02").isEqualTo(Decimal.fromString("1265.01")));
        assert(Decimal.fromString("1,265.01").isEqualTo(Decimal.fromString("1265.01")));
        assert(Decimal.fromString("1265,01").isEqualTo(Decimal.fromString("1265.01")));
        assert(Decimal.fromString("1265,0").isEqualTo(Decimal.fromString("1265")));
    }

    @Test
    public void canInvert()
    {
        assert(Decimal.fromString("0.5").isEqualTo(Decimal.fromString("2").invert()));
        assert(Decimal.fromString("10").isEqualTo(Decimal.fromString("0.1").invert()));
    }

        @Test
    public void canRound()
    {
        Decimal r1 = Decimal.fromDouble(1.0003).roundToTickSize(tickSize0001);
        assert(r1.isEqualTo(Decimal.fromString("1")));
        Decimal r2 = Decimal.fromDouble(1.0008).roundToTickSize(tickSize0001);
        assert(r2.isEqualTo(Decimal.fromString("1.001")));

        Decimal r3 = Decimal.fromDouble(1.003).roundToTickSize(tickSize001);
        assert(r3.isEqualTo(Decimal.fromString("1")));
        Decimal r4 = Decimal.fromDouble(1.008).roundToTickSize(tickSize001);
        assert(r4.isEqualTo(Decimal.fromString("1.01")));

        Decimal r5 = Decimal.fromDouble(1.003).roundToTickSize(tickSize005);
        assert(r5.isEqualTo(Decimal.fromString("1")));
        Decimal r6 = Decimal.fromDouble(1.03).roundToTickSize(tickSize005);
        assert(r6.isEqualTo(Decimal.fromString("1.05")));

        Decimal r7 = Decimal.fromDouble(1.03).roundToTickSize(tickSize01);
        assert(r7.isEqualTo(Decimal.fromString("1")));
        Decimal r8 = Decimal.fromDouble(1.08).roundToTickSize(tickSize01);
        assert(r8.isEqualTo(Decimal.fromString("1.1")));

        Decimal r9 = Decimal.fromDouble(1.3).roundToTickSize(tickSize05);
        assert(r9.isEqualTo(Decimal.fromString("1.5")));
        Decimal r10 = Decimal.fromDouble(1.8).roundToTickSize(tickSize05);
        assert(r10.isEqualTo(Decimal.fromString("2")));

        Decimal r11 = Decimal.fromDouble(3).roundToTickSize(tickSize5);
        assert(r11.isEqualTo(Decimal.fromString("5")));
        Decimal r12 = Decimal.fromDouble(8).roundToTickSize(tickSize5);
        assert(r12.isEqualTo(Decimal.fromString("10")));

    }



    @BeforeMethod
    public void setUp() {
        tickSize0001 = new Decimal("0.001");
        tickSize001 = new Decimal("0.01");
        tickSize005 = new Decimal("0.05");
        tickSize01 = new Decimal("0.1");
        tickSize05 = new Decimal("0.5");
        tickSize1 = new Decimal("1");
        tickSize5 = new Decimal("5");
    }

    Decimal tickSize0001;
    Decimal tickSize001;
    Decimal tickSize005;
    Decimal tickSize01;
    Decimal tickSize05;
    Decimal tickSize1;
    Decimal tickSize5;


} // class
