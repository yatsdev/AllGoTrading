package org.yats.trading;

import org.joda.time.DateTime;
import org.testng.annotations.Test;
import org.yats.common.Decimal;

import java.util.Vector;

/**
 * Created by emiliano on 25/05/2014.
 */
public class ProductStaticDataTest {

    Product product=new Product().withProductId("4663743").withName("Wall Mart Stores, Inc").withSymbol("WMT").withExchange("XNAS");
    ProductStaticData productStaticData=new ProductStaticData();
    TargetPrice TP=new TargetPrice();
    Vector<TargetPrice> TPVector=new Vector();
    Product productDerivative=new Product().withProductId("46637432").withName("WMT 70 Strike Call Option").withSymbol("WMT140530C00070000").withExchange("XNAS");//Product not present in the CDF list, its PID is fictional



    @Test
    public void canUseProductStaticData() {
        productStaticData.setProductDescription("Wal-Mart Stores, Inc. operates discount stores, supercenters, and neighborhood markets. The Company's discount stores and supercenters offer merchandise such as apparel, housewares, small appliances, electronics, and hardware. Walmart's markets offer a full-line supermarket and a limited assortment of general merchandise. The Company operates nationally and internationally."); //Bloomberg description
        productStaticData.setProductType("Equity");

        assert(productStaticData.isHasDerivatives()==false);

        productStaticData.setDerivative(productDerivative);
        TP.setIssuer("Goldman Sachs");//http://www.analystratings.net/stocks/NYSE/WMT/
        TP.setPrice(Decimal.fromDouble(83));//http://www.analystratings.net/stocks/NYSE/WMT/
        TP.setTPDateTime(DateTime.parse("2012-05-16"));//http://www.analystratings.net/stocks/NYSE/WMT/
        TPVector.add(TP);
       productStaticData.setTP(TPVector);

        assert(!(productStaticData.getTP()==null));
        assert(productStaticData.isHasDerivatives()==true);
        assert(productStaticData.getProductType().equals("Equity"));


    }





}
