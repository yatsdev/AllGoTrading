package org.yats.trading;

import org.yats.common.Decimal;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by emiliano on 26/05/2014.
 */
public class ConcurrentXYTable {

    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Decimal>> matrix;


    public ConcurrentXYTable() {
        matrix = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Decimal>>();
    }

    public Decimal getXY(Integer x, Integer y) {
       /*
        ConcurrentHashMap<Integer, Decimal> concurrentHashMap = new ConcurrentHashMap<Integer, Decimal>();
        concurrentHashMap.put(y, Decimal.ZERO);
        matrix.put(x, concurrentHashMap);
      */
        Decimal elementAtXY = Decimal.ZERO;
        ConcurrentHashMap workingConcurrentHashMap = matrix.get(x);
        elementAtXY = new Decimal(workingConcurrentHashMap.get(y).toString());
        return elementAtXY;
    }

    public void setXY(Integer x, Integer y, Decimal content) {
        ConcurrentHashMap<Integer, Decimal> concurrentHashMap = new ConcurrentHashMap<Integer, Decimal>();
        concurrentHashMap.put(y, Decimal.ZERO);
        matrix.put(x, concurrentHashMap);

        ConcurrentHashMap workingConcurrentHashMap = matrix.get(x);
        workingConcurrentHashMap.put(y, content);
    }


}
