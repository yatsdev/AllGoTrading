package org.yats.trading;

import java.util.concurrent.ConcurrentHashMap;



public class ProfitSnapshot {

    public AccountPosition getProductAccountPosition(String prod1, String account1) {
        throw new RuntimeException("not yet implemented!");
    }

    public void add(ProductAccountProfit p) {
        ProductAccountProfit newProfit = p;
        String key = p.getKey();
        if(profitMap.containsKey(key)) {
            ProductAccountProfit oldProfit = profitMap.get(key);
            newProfit=oldProfit.add(newProfit);
        }
        profitMap.put(key, newProfit);
    }

    public ProfitSnapshot() {
        profitMap = new ConcurrentHashMap<String, ProductAccountProfit>();
    }

    ConcurrentHashMap<String, ProductAccountProfit> profitMap;
} // class
