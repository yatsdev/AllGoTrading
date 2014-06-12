package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {

        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.get(position.getProductId()).getLast();
        Decimal positionSizedInTargetCurrency=null;

        Product productToConvert=null;

        if(isSameCurrency(position,targetProductId)){
            positionSizedInTargetCurrency=getMarketDataForProduct(position.getProductId()).getLast().multiply(position.getSize());
        }

        else {

            if (targetProductId.compareTo("CCY007") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY001")==0)) { //from USD to EUR

                positionInTargetCurrency = priceInOriginalCurrency.divide(rates.get("OANDA0001").getLast());

            }

            if (targetProductId.compareTo("CCY001") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY007")==0)) { //from EUR to USD

                positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get("OANDA0001").getLast());

            }

            if (targetProductId.compareTo("CCY004") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY007")==0)) { //from EUR to CHF) {

                positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get("OANDA0003").getLast());

            }

            if (targetProductId.compareTo("CCY007") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY004")==0)) { //from CHF to EUR) {

                positionInTargetCurrency = priceInOriginalCurrency.divide(rates.get("OANDA0003").getLast());

            }

            if (targetProductId.compareTo("CCY001") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY004")==0)) { //from USD to CHF) {

                positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get("OANDA0004").getLast());

            }

            if (targetProductId.compareTo("CCY004") == 0 && (products.getProductForProductId(position.getProductId()).getUnitId().compareTo("CCY001")==0)) { //from CHF to USD) {

                positionInTargetCurrency = priceInOriginalCurrency.divide(rates.get("OANDA0004").getLast());

            }






        }


        return new Position(targetProductId, positionInTargetCurrency);
    }

    public boolean isSameCurrency(Position position,String targetProductId){
        if(products.getProductForProductId(position.getProductId()).getUnitId().toString().compareTo(targetProductId)==0) return true;
        else return false;
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
