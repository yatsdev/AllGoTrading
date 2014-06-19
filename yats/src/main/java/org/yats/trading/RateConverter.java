package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


      public Position convert(Position position, String targetProductId) {



        Decimal positionInTargetCurrency = null;
        Decimal priceInOriginalCurrency = rates.get(position.getProductId()).getLast();
        Decimal positionSizedInTargetCurrency=null;

        Collection<Product> collection;

        String originalProductID=products.getProductForProductId(position.getProductId()).getUnitId().toString();

        if(originalProductID.compareTo(targetProductId)==0){ //Case 0: original and target currencies are the same
            positionInTargetCurrency=getMarketDataForProduct(position.getProductId()).getLast();
        }

        else {    //Case 1: Original and target currencies differ


            collection =  products.getProductsWithUnderlying(originalProductID).getProductsWithUnit(targetProductId).values();  //here I select suitable products that can convert from the originalProduct to the targetProduct
            Iterator itr = collection.iterator();
            while(itr.hasNext()){
                    Product pair= (Product) itr.next();
                 positionInTargetCurrency = priceInOriginalCurrency.multiply(rates.get(pair.getProductId()).getLast());
             }

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

     private MarketData getMarketDataForProduct(String pid) {
        if(!rates.containsKey(pid)) throw new TradingExceptions.ItemNotFoundException("Can not find rate for pid="+pid);
        return rates.get(pid);
    }

    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class
