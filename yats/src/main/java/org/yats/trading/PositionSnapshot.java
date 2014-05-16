package org.yats.trading;

import java.util.HashMap;

public class PositionSnapshot {

    public ProductAccountPosition getProductAccountPosition(String prod1, String account1) {
       
       System.out.println(positionMap.size());  // returns 1
        String key= new String("1");             
        System.out.println(positionMap.toString()); //returns {1,intAccount1=org.yats.trading.ProductAccountPosition@5ce65a89}
        System.out.println(positionMap.get(key));   //returns null
        System.out.println(positionMap.containsKey(key)); //returns false



        ProductAccountPosition productAccountPosition=new ProductAccountPosition("","", Decimal.ONE);
        return productAccountPosition;
       
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
