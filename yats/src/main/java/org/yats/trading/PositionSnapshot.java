package org.yats.trading;

import java.util.HashMap;

public class PositionSnapshot {

    public ProductAccountPosition getProductAccountPosition(String prod1, String account1) {
        throw new RuntimeException("not yet implemented!");
    }

    public void add(ProductAccountPosition p) {
        ProductAccountPosition newPosition = p;
        String key = p.getKey();
        if(positionMap.containsKey(key)) {
            ProductAccountPosition oldPosition = positionMap.get(key);
            newPosition=oldPosition.add(newPosition);
        }
        positionMap.put(key, newPosition);
   }

    public PositionSnapshot() {
        positionMap = new HashMap<String, ProductAccountPosition>();
    }

    //    LinkedList<ProductAccountPosition> positionList;
    HashMap<String, ProductAccountPosition> positionMap;


} // class
