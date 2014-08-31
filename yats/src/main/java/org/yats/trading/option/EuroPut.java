package org.yats.trading.option;

import org.joda.time.DateTime;

public class EuroPut extends OptionBase {

    public EuroPut(double strikePrice, DateTime expiration) {
        this.strikePrice = strikePrice;
        this.expiration = expiration;
    }

    public double getPrice(Parameter p)
    {
        double expTime = p.getTimeToExpirationAsFractionOfYear(expiration);
        double time_sqrt = Math.sqrt(expTime);
        double d1 = calcD1(p);
        double d2 = d1-(p.getSigma()*time_sqrt);
        return strikePrice*Math.exp(-p.getInterestRate()*expTime)*calcNormalized(-d2)
                - p.getSpotPrice()*calcNormalized(-d1);
    }

    public Greeks getGreeks(Parameter p)
    {
        double expTime = p.getTimeToExpirationAsFractionOfYear(expiration);
        double time_sqrt = Math.sqrt(expTime);
        double d1 = calcD1(p);
        double d2 = d1-(p.getSigma()*time_sqrt);
        double delta = -calcNormalized(-d1); // *contractSize();
        double gamma = calcNormalDistribution(d1)/(p.getSpotPrice()*p.getSigma()*time_sqrt); //*contractSize();
        double thetaPerYear = -(p.getSpotPrice()*p.getSigma()*calcNormalDistribution(d1))
                / (2*time_sqrt)+ p.getInterestRate()*strikePrice * Math.exp(-p.getInterestRate()*expTime) * calcNormalized(-d2);
        double vega  = p.getSpotPrice() * time_sqrt * calcNormalDistribution(d1);
        double rho   = -strikePrice*expTime*Math.exp(-p.getInterestRate()*expTime) * calcNormalized(-d2);
        return new Greeks(delta,gamma,thetaPerYear,vega,rho);
    }

    private double calcD1(Parameter p)
    {
        double time_sqrt = Math.sqrt(p.getTimeToExpirationAsFractionOfYear(expiration));
        double d1 = (Math.log(p.getSpotPrice()/strikePrice)+p.getInterestRate()*p.getTimeToExpirationAsFractionOfYear(expiration))/(p.getSigma()*time_sqrt) + 0.5*p.getSigma()*time_sqrt;
        return d1;
    }

    private double strikePrice;
    private DateTime expiration;



} // class
