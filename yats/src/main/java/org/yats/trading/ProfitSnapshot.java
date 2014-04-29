package org.yats.trading;

import java.util.HashMap;

// todo: allow for multiple currencies. assumption so far: all trading happens in same currency as the accounting currency

public class ProfitSnapshot {

    public ProductAccountPosition getProductAccountPosition(String prod1, String account1) {
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
        profitMap = new HashMap<String, ProductAccountProfit>();
    }

    HashMap<String, ProductAccountProfit> profitMap;
} // class
