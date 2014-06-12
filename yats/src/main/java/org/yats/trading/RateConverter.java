package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


     public Position convert(Position position, String targetProductId) {

        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.get(position.getProductId()).getLast();
        Decimal positionSizedInTargetCurrency=null;


     //   if (targetProductId.compareTo("CCY007") == 0) { //here I should skip this code because SAP is already in EUR..what I miss is how do I know that 4663789 is already in EUR?

       //     positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get("OANDA0001").getLast());

      //  }



       positionSizedInTargetCurrency=getMarketDataForProduct(position.getProductId()).getLast().multiply(position.getSize());
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

     private MarketData getMarketDataForProduct(String pid) {
        if(!rates.containsKey(pid)) throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid="+pid);
        return rates.get(pid);
    }

    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class
