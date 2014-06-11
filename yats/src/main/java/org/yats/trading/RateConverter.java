package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {
   Decimal positionInTargetCurrency = null;

        if(targetProductId.compareTo("CCY007")==0){

            System.out.println(position.getSize());
            System.out.println(rates.get("OANDA0001").getLast());
            positionInTargetCurrency=position.getSize().multiply(rates.get("OANDA0001").getLast());

        }

        
      
        return new Position(targetProductId, positionInTargetCurrency);
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


    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class
