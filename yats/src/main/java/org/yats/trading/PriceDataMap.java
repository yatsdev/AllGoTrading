package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentHashMap;

public class PriceDataMap {

    public boolean containsKey(String productId) {
        return rates.containsKey(productId);
    }

    public Decimal getLastPrice(String productId) {
        return get(productId).getLast();
    }

    public PriceData get(String productId) {
        if(!containsKey(productId)) throw new CommonExceptions.KeyNotFoundException("Can not find key "+productId);
        return rates.get(productId);
    }

    public void put(String productId, PriceData priceData) {
        rates.put(productId, priceData);
    }

    public PriceDataMap() {
        rates = new ConcurrentHashMap<String, PriceData>();
    }

    private ConcurrentHashMap<String, PriceData> rates;


} // class
