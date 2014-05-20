package org.yats.trading;

import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshot {

    //    LinkedList<ProductAccountPosition> positionList;
    ConcurrentHashMap<String, ProductAccountPosition> positionMap;

    public PositionSnapshot() {
        positionMap = new ConcurrentHashMap<String, ProductAccountPosition>();
    }

    public ProductAccountPosition getProductAccountPosition(String prod1, String account1) {

        ProductAccountPosition position = new ProductAccountPosition("","", Decimal.ZERO);

        String key = prod1 + "," + account1;
        if (positionMap.containsKey(key)) {
            position = positionMap.get(key);

        }

        return position;
    }

    public void add(ProductAccountPosition p) {
        ProductAccountPosition newPosition = p;
        String key = p.getKey();
        if (positionMap.containsKey(key)) {
            ProductAccountPosition oldPosition = positionMap.get(key);
            newPosition = oldPosition.add(newPosition);
        }
        positionMap.put(key, newPosition);
    }


} // class
