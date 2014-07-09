package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentHashMap;

public class MarketDataMap {

    public boolean containsKey(String productId) {
        return rates.containsKey(productId);
    }

    public Decimal getLastPrice(String productId) {
        return get(productId).getLast();
    }

    public MarketData get(String productId) {
        if(!containsKey(productId)) throw new CommonExceptions.KeyNotFoundException("Can not find key "+productId);
        return rates.get(productId);
    }

    public void put(String productId, MarketData marketData) {
        rates.put(productId, marketData);
    }

    public MarketDataMap() {
        rates = new ConcurrentHashMap<String, MarketData>();
    }

    private ConcurrentHashMap<String, MarketData> rates;


} // class
