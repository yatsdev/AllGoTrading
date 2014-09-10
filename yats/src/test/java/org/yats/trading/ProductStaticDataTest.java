package org.yats.trading;

import org.joda.time.DateTime;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

import java.util.Vector;

public class ProductStaticDataTest {

    Product product=new Product().withProductId("4663743").withName("Wall Mart Stores, Inc").withSymbol("WMT").withExchange("XNAS");
    ProductStaticData productStaticData=new ProductStaticData();
    TargetPrice TP1=new TargetPrice();
    TargetPrice TP2=new TargetPrice();
    TargetPrice TP3=new TargetPrice();
    TargetPrice TP4=new TargetPrice();
    Vector<TargetPrice> TPVector=new Vector();
    Product productDerivative=new Product().withProductId("46637432").withName("WMT 70 Strike Call Option").withSymbol("WMT140530C00070000").withExchange("XNAS");//Product not present in the CDF list, its PID is fictional



    @Test
    public void canUseProductStaticData() {
        productStaticData.setProductDescription("Wal-Mart Stores, Inc. operates discount stores, supercenters, and neighborhood markets. The Company's discount stores and supercenters offer merchandise such as apparel, housewares, small appliances, electronics, and hardware. Walmart's markets offer a full-line supermarket and a limited assortment of general merchandise. The Company operates nationally and internationally."); //Bloomberg description
        productStaticData.setProductType("Equity");

        assert(!productStaticData.isHasDerivatives());

        productStaticData.setDerivative(productDerivative);

        TP1.setIssuer("Goldman Sachs");//http://www.analystratings.net/stocks/NYSE/WMT/
        TP1.setPrice(Decimal.fromDouble(83));//http://www.analystratings.net/stocks/NYSE/WMT/
        TP1.setTPDateTime(DateTime.parse("2014-05-16"));//http://www.analystratings.net/stocks/NYSE/WMT/
        TPVector.add(TP1);

        TP2.setIssuer("Raymond James");
        TP2.setPrice(Decimal.fromDouble(83));
        TP2.setTPDateTime(DateTime.parse("2014-05-16"));
        TPVector.add(TP2);

        TP3.setIssuer("Deutsche Bank");
        TP3.setPrice(Decimal.fromDouble(75));
        TP3.setTPDateTime(DateTime.parse("2014-05-16"));
        TPVector.add(TP3);

        TP4.setIssuer("MKM Partners");
        TP4.setPrice(Decimal.fromDouble(75));
        TP4.setTPDateTime(DateTime.parse("2014-05-16"));
        TPVector.add(TP4);

        productStaticData.setTP(TPVector);

        assert(!(productStaticData.getTP()==null));
        assert(productStaticData.isHasDerivatives());
        assert(productStaticData.getProductType().equals("Equity"));
        assert(productStaticData.getAvarageTargetPrice().isEqualTo(Decimal.fromDouble(79)));


    }





}
