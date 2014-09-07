package org.yats.trading;

import org.joda.time.DateTime;
import org.yats.common.Decimal;

public class TargetPrice {
    private DateTime TPDateTime;
    private String issuer;
    private Decimal price;


    public DateTime getTPDateTime() {
        return TPDateTime;
    }

    public void setTPDateTime(DateTime TPDateTime) {
        this.TPDateTime = TPDateTime;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Decimal getPrice() {
        return price;
    }

    public void setPrice(Decimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "TargetPrice{" +
                "TPDateTime=" + TPDateTime +
                ", issuer='" + issuer + '\'' +
                ", price=" + price +
                '}';
    }
}
