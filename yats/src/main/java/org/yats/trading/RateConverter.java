package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {

        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.get(position.getProductId()).getLast();
        Decimal positionSizedInTargetCurrency=null;

        final String EUR=new String("CCY007");
        final String USD=new String("CCY001");
        final String CHF=new String("CCY004");
        final String GBP=new String("CCY011");

        final String EUR_USD=new String("OANDA0001");
        final String EUR_GBP=new String("OANDA0002");
        final String EUR_CHF=new String("OANDA0003");
        final String USD_CHF=new String("OANDA0004");



        if(isSameCurrency(position,targetProductId)){
            positionInTargetCurrency=getMarketDataForProduct(position.getProductId()).getLast();
        }

        else {

            positionInTargetCurrency = returnPositionInTargetCurrency(targetProductId,position,EUR,USD,priceInOriginalCurrency,positionInTargetCurrency,EUR_USD);
            positionInTargetCurrency = returnPositionInTargetCurrency(targetProductId,position,EUR,CHF,priceInOriginalCurrency,positionInTargetCurrency,EUR_CHF);
              //still need to place other pairs here..is this one better than the previous?
              

        }

        positionSizedInTargetCurrency= positionInTargetCurrency.multiply(position.getSize());

        return new Position(targetProductId, positionSizedInTargetCurrency);
    }


    public boolean isSameCurrency(Position position,String targetProductId){
        if(products.getProductForProductId(position.getProductId()).getUnitId().toString().compareTo(targetProductId)==0) return true;
        else return false;
        }


    public Decimal returnPositionInTargetCurrency(String targetProductId,Position position, String fromCurrency, String toCurrency, Decimal priceInOriginalCurrency,Decimal positionInTargetCurrency,String OANDAFXPAIR){

        if (targetProductId.compareTo(toCurrency) == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo(fromCurrency)==0))
        {
           positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get(OANDAFXPAIR).getLast());
        }

        return positionInTargetCurrency;
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

     private MarketData getMarketDataForProduct(String pid) {
        if(!rates.containsKey(pid)) throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid="+pid);
        return rates.get(pid);
    }

    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class
