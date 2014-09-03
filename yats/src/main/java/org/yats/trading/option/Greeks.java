package org.yats.trading.option;

public class Greeks {

    @Override
    public String toString() {
        return "Greeks{" +
                "delta=" + delta +
                ", gamma=" + gamma +
                ", theta=" + getThetaPerDay365Year() +
                ", thetaPerYear=" + thetaPerYear +
                '}';
    }

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

    public double getThetaPerDay365Year()
    {
        return thetaPerYear / 365;
    }

//    public double getVega() {         // probably wrong!
//        return vega;
//    }

//    public double getRho() {          // probably wrong!
//        return rho;
//    }

    double delta; //  partial wrt S
    double gamma; //  second prt wrt S
    double thetaPerYear; // partial wrt time
    double vega;  //  partial wrt sigma
    double rho;   // partial wrt r



} // class
