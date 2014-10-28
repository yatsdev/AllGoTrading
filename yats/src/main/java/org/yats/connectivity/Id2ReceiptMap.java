package org.yats.connectivity;

import org.yats.trading.Receipt;

import java.util.concurrent.ConcurrentHashMap;

public class Id2ReceiptMap {

    public void putOrderId2ExternalIdMapping(String orderId, String externalId) {
        orderId2OandaIdMap.put(orderId, externalId);
    }

    public int size() {
        return orderId2OandaIdMap.size();
    }

    public String get(String orderId) {
        return orderId2OandaIdMap.get(orderId);
    }

    public Id2ReceiptMap() {
        oandaId2OrderMapFilename="";
        orderId2OandaIdMapFilename="";
        oandaId2Receipt=new ConcurrentHashMap<String, Receipt>();
        orderId2OandaIdMap=new ConcurrentHashMap<String, String>();

    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    private ConcurrentHashMap<String, Receipt> oandaId2Receipt;
    private ConcurrentHashMap<String, String> orderId2OandaIdMap;
    private String orderId2OandaIdMapFilename;
    private String oandaId2OrderMapFilename;


} // class
