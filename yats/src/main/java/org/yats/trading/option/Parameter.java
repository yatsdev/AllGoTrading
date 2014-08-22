package org.yats.trading.option;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Parameter {

    public Parameter(double _spotPrice, double _interestRate, double _sigma, DateTime _valuationTime) {
        this.spotPrice = _spotPrice;
        this.interestRate = _interestRate;
        this.sigma = _sigma;
        this.valuationTime = _valuationTime;
    }

    public double getSpotPrice() {
        return spotPrice;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getSigma() {
        return sigma;
    }

    public DateTime getValuationTime() {
        return valuationTime;
    }

    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public void setValuationTime(DateTime _valuationTime) {
        this.valuationTime = _valuationTime;
    }

    public double getTimeToExpirationAsFractionOfYear(DateTime expirationTime)
    {
        if(valuationTime.isAfter(expirationTime)) return 0;
        Interval interval = new Interval(valuationTime, expirationTime);

        double yearMillis = Duration.standardDays(365).getMillis();
        double millis = interval.toDurationMillis();
        return millis / yearMillis;
    }

    private double spotPrice;         // also called S
    private double interestRate;      // also called r
    private double sigma;   			  // volatility
    private DateTime valuationTime;


} // class
