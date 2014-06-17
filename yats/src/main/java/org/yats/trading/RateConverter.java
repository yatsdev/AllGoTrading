package org.yats.trading;

import org.yats.common.UniqueId;
import org.yats.common.Decimal;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {

        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.get(position.getProductId()).getLast();
        Decimal positionSizedInTargetCurrency=null;

        String originalProductID=products.getProductForProductId(position.getProductId()).getUnitId().toString();

        Vector<pair> vectorPairs = new Vector<pair>();

        pair EURUSD=new pair();
        EURUSD.setFromCurrency("CCY_EUR");
        EURUSD.setToCurrency("CCY_USD");
        EURUSD.setPairName("OANDA_EURUSD");
        vectorPairs.add(EURUSD);


        pair EURGBP=new pair();
        EURGBP.setFromCurrency("CCY_EUR");
        EURGBP.setToCurrency("CCY_GBP");
        EURGBP.setPairName("OANDA_EURGBP");
        vectorPairs.add(EURGBP);

        pair EURCHF=new pair();
        EURCHF.setFromCurrency("CCY_EUR");
        EURCHF.setToCurrency("CCY_CHF");
        EURCHF.setPairName("OANDA_EURCHF");
        vectorPairs.add(EURCHF);

        pair XAUUSD=new pair();
        XAUUSD.setFromCurrency("CCY_XAU");
        XAUUSD.setToCurrency("CCY_USD");
        XAUUSD.setPairName("OANDA_XAUUSD");
        vectorPairs.add(XAUUSD);

        pair XAUXAG=new pair();
        XAUXAG.setFromCurrency("CCY_XAU");
        XAUXAG.setToCurrency("CCY_XAG");
        XAUXAG.setPairName("OANDA_XAUXAG");
        vectorPairs.add(XAUXAG);


        pair XAGUSD=new pair();
        XAGUSD.setFromCurrency("CCY_XAG");
        XAGUSD.setToCurrency("CCY_USD");
        XAGUSD.setPairName("OANDA_XAGUSD");
        vectorPairs.add(XAGUSD);


        pair XAGNZD=new pair();
        XAGNZD.setFromCurrency("CCY_XAG");
        XAGNZD.setToCurrency("CCY_NZD");
        XAGNZD.setPairName("OANDA_XAGNZD");
        vectorPairs.add(XAGNZD);

        pair AUDCHF=new pair();
        AUDCHF.setFromCurrency("CCY_AUD");
        AUDCHF.setToCurrency("CCY_CHF");
        AUDCHF.setPairName("OANDA_AUDCHF");
        vectorPairs.add(AUDCHF);

        pair AUDHKD=new pair();
        AUDHKD.setFromCurrency("CCY_AUD");
        AUDHKD.setToCurrency("CCY_HKD");
        AUDHKD.setPairName("OANDA_AUDHKD");
        vectorPairs.add(AUDHKD);

        pair CADHKD=new pair();
        CADHKD.setFromCurrency("CCY_CAD");
        CADHKD.setToCurrency("CCY_HKD");
        CADHKD.setPairName("OANDA_CADHKD");
        vectorPairs.add(CADHKD);

        pair CADSGD=new pair();
        CADSGD.setFromCurrency("CCY_CAD");
        CADSGD.setToCurrency("CCY_SGD");
        CADSGD.setPairName("OANDA_CADSGD");
        vectorPairs.add(CADSGD);

        pair NZDCAD=new pair();
        NZDCAD.setFromCurrency("CCY_NZD");
        NZDCAD.setToCurrency("CCY_CAD");
        NZDCAD.setPairName("OANDA_NZDCAD");
        vectorPairs.add(NZDCAD);

        pair SGDHKD=new pair();
        SGDHKD.setFromCurrency("CCY_SGD");
        SGDHKD.setToCurrency("CCY_HKD");
        SGDHKD.setPairName("OANDA_SGDHKD");
        vectorPairs.add(SGDHKD);





        if(originalProductID.compareTo(targetProductId)==0){ //Case 0: original and target currencies are the same
            positionInTargetCurrency=getMarketDataForProduct(position.getProductId()).getLast();
        }

        else {    //Case 1: Original and target currencies differ but there's a direct pair converting them


            for(int i=0;i<vectorPairs.size();i++){

            if(vectorPairs.elementAt(i).getFromCurrency().compareTo(originalProductID)==0&&vectorPairs.elementAt(i).getToCurrency().compareTo(targetProductId)==0) {

                positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get(vectorPairs.elementAt(i).getPairName()).getLast());
            }   //if this if statement fails I need to write case 2..but I don't know how to put the else statement because the external "for" statmentes creates me problems.
                

            }
                 //I need help here.. I'm writing case 2 but I don't exactly know how to direct the data flow.
                 

        }

        positionSizedInTargetCurrency= positionInTargetCurrency.multiply(position.getSize());

        return new Position(targetProductId, positionSizedInTargetCurrency);
    }


    @Override
    public void onMarketData(MarketData marketData) {
        rates.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    public RateConverter(IProvideProduct p) {
        products = p;
        rates = new ConcurrentHashMap<String, MarketData>();
    }

    public MarketData getMarketDataForProduct(String pid) {
        if(!rates.containsKey(pid)) throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid="+pid);
        return rates.get(pid);
    }

    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class

class pair{

    private boolean hasSon=false;
    private String fromCurrency;
    private String toCurrency;
    private String pairName;
    private pair son;

    public pair getSon() {
        return son;
    }

    public void setSon(pair son) {
        hasSon=true;
        this.son = son;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }
}
