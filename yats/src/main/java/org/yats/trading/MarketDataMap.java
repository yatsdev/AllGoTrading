package org.yats.trading;

import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentHashMap;

public class MarketDataMap {

    public boolean containsKey(String productId) {
        return rates.containsKey(productId);
    }

    public MarketData get(String productId) {
        return rates.get(productId);
    }

    public Decimal getLastPrice(String productId) {
        return rates.get(productId).getLast();
    }

    public void put(String productId, MarketData marketData) {
        rates.put(productId, marketData);
    }


    public MarketDataMap() {
        rates = new ConcurrentHashMap<String, MarketData>();
    }

    private ConcurrentHashMap<String, MarketData> rates;


} // class
