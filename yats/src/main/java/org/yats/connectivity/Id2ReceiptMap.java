package org.yats.connectivity;

import org.yats.trading.Receipt;

import java.util.concurrent.ConcurrentHashMap;

public class Id2ReceiptMap {


    private ConcurrentHashMap<String, Receipt> oandaId2Receipt;
    private ConcurrentHashMap<String, String> orderId2OandaIdMap;
    private String orderId2OandaIdMapFilename;
    private String oandaId2OrderMapFilename;

} // class
