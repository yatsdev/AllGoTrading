package org.yats.trading.option;

public class Greeks {

    public Greeks(double delta, double gamma, double thetaPerYear, double vega, double rho) {
        this.delta = delta;
        this.gamma = gamma;
        this.thetaPerYear = thetaPerYear;
        this.vega = vega;
        this.rho = rho;
    }

    public double getDelta() {
        return delta;
    }

    public double getGamma() {
        return gamma;
    }

    public double getThetaPerYear() {
        return thetaPerYear;
    }

    double getThetaPerDay365Year()
    {
        return thetaPerYear / 365;
    }

    public double getVega() {
        return vega;
    }

    public double getRho() {
        return rho;
    }

    double delta; //  partial wrt S
    double gamma; //  second prt wrt S
    double thetaPerYear; // partial wrt time
    double vega;  //  partial wrt sigma
    double rho;   // partial wrt r



} // class
