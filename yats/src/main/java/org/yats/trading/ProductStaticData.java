package org.yats.trading;

import org.yats.common.Decimal;

import java.util.Vector;

/**
 * Created by emiliano on 25/05/2014.
 */
public class ProductStaticData extends Product {
    private String productDescription;
    private String productType;
    private Product derivative;
    private boolean hasDerivatives = false;
    private Vector<TargetPrice> TP;
    private Decimal avarageTargetPrice;


    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Product getDerivative() {
        return derivative;
    }

    public boolean isHasDerivatives() {
        return hasDerivatives;
    }

    public void setHasDerivatives(boolean hasDerivatives) {
        this.hasDerivatives = hasDerivatives;
    }

    public void setDerivative(Product derivative) {
        this.derivative = derivative;
        hasDerivatives = true;

    }

    @Override
    public String toString() {
        return "ProductStaticData{" +
                "productDescription='" + productDescription + '\'' +
                ", productType='" + productType + '\'' +
                ", derivative=" + derivative +
                ", hasDerivatives=" + hasDerivatives +
                ", TP=" + TP +
                '}';
    }

    public Vector<TargetPrice> getTP() {
        return TP;
    }

    public Decimal getAvarageTargetPrice() {
        return avarageTargetPrice;
    }

    public void setAvarageTargetPrice(Decimal avarageTargetPrice) {
        this.avarageTargetPrice = avarageTargetPrice;
    }

    public void setTP(Vector<TargetPrice> TP) {
        this.TP = TP;
        Decimal sum=Decimal.ZERO;
        for(int i=0;i<TP.size();i++){

            sum= TP.elementAt(i).getPrice().add(sum);
            avarageTargetPrice=sum.divide(Decimal.fromDouble(TP.size()));
        }

    }
}