package org.yats.connectivity;

import org.yats.common.FileTool;
import org.yats.messagebus.Deserializer;
import org.yats.messagebus.Serializer;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trading.Receipt;

import java.util.concurrent.ConcurrentHashMap;

public class Id2ReceiptMap {

    public void putOrderId2ExternalIdMapping(String orderId, String externalId) {
        orderId2ExternalIdMap.put(orderId, externalId);
    }

    public int sizeOrderIds() {
        return orderId2ExternalIdMap.size();
    }

    public int sizeExternalIds() {
        return externalId2Receipt.size();
    }


    public String getExternalId(String orderId) {
        return orderId2ExternalIdMap.get(orderId);
    }

    public Receipt getReceipt(String orderId) {
        String externalId = orderId2ExternalIdMap.get(orderId);
        return getReceiptForExternalId(externalId);
    }

    public Receipt getReceiptForExternalId(String externalId) {
        return externalId2Receipt.get(externalId).toReceipt();
    }

    public boolean containsReceiptForOrderId(String orderId) {
        if(!orderId2ExternalIdMap.containsKey(orderId)) return false;
        String externalId = orderId2ExternalIdMap.get(orderId);
        return externalId2Receipt.containsKey(externalId);
    }

    public void putReceipt(String externalId, Receipt receipt) {
        externalId2Receipt.put(externalId, ReceiptMsg.fromReceipt(receipt));
    }

    public boolean containsReceiptForExternalId(String externalId) {
        return externalId2Receipt.containsKey(externalId);
    }

    public void remove(String orderId) {
        if(!orderId2ExternalIdMap.containsKey(orderId)) return;
        String externalId = orderId2ExternalIdMap.get(orderId);
        orderId2ExternalIdMap.remove(orderId);
        if(!externalId2Receipt.containsKey(externalId)) return;
        externalId2Receipt.remove(externalId);
    }


    public String toStringJSon() {
        Serializer serializer = new Serializer();
        return serializer.convertToString(this);
    }

    public static Id2ReceiptMap createFromStringJson(String data) {
        if(data.length()==0) return new Id2ReceiptMap();
        Deserializer<Id2ReceiptMap> deserializer = new Deserializer<Id2ReceiptMap>(Id2ReceiptMap.class);
        return deserializer.convertFromString(data);
    }

    public static Id2ReceiptMap createFromFile(String filename) {
        String data = FileTool.exists(filename)
                ? FileTool.readFromTextFile(filename)
                : "";
        Deserializer<Id2ReceiptMap> deserializer = new Deserializer<Id2ReceiptMap>(Id2ReceiptMap.class);
        return deserializer.convertFromString(data);
    }

//    @Deprecated
//    public void storeToDisk() {
//
//        writeFileOrderId2OandaIdMap();
//        writeFileOandaId2ReceiptMap();
//    }
//
//    @Deprecated
//    public void readFromDisk() {
//        init();
//        readFileOrderId2OandaIdMap();
//        readFileOandaId2ReceiptMap();
//    }

    public static Id2ReceiptMap createFromFileJson(String filename) {
        String data = FileTool.exists(filename)
                ? FileTool.readFromTextFile(filename)
                : "";
        return createFromStringJson(data);
    }

//    @Deprecated
//    public void readFileOrderId2OandaIdMap() {
//        String csvString = FileTool.exists(orderId2ExternalIdMapFilename)
//                ? FileTool.readFromTextFile(orderId2ExternalIdMapFilename)
//                : "";
//        parseOrderId2ExternalIdMap(csvString);
//    }
//
//    @Deprecated
//    public void readFileOandaId2ReceiptMap() {
//        String csvString = FileTool.exists(externalId2OrderMapFilename)
//                ? FileTool.readFromTextFile(externalId2OrderMapFilename)
//                : "";
//        parseExternalId2OrderMap(csvString);
//    }

    public void writeToFileJson(String filename) {
        String data = toStringJSon();
        FileTool.writeToTextFile(filename, data, false);
    }

//    @Deprecated
//    public void writeFileOandaId2ReceiptMap() {
//        String csvString = toStringCSVExternalId2OrderMap();
//        FileTool.writeToTextFile(externalId2OrderMapFilename, csvString, false);
//    }
//
//    @Deprecated
//    public void writeFileOrderId2OandaIdMap() {
//        String csvString = toStringCSVOrderId2ExternalIdMap();
//        FileTool.writeToTextFile(orderId2ExternalIdMapFilename, csvString, false);
//    }

//    @Deprecated
//    public String toStringCSVExternalId2OrderMap() {
//        StringBuilder b = new StringBuilder();
//        for(String key : externalId2Receipt.keySet()) {
//            Receipt o = externalId2Receipt.get(key).toReceipt();
//            b.append(key); b.append(";");
//            Serializer<ReceiptMsg> serializer = new Serializer<ReceiptMsg>();
//            String s = serializer.convertToString(ReceiptMsg.fromReceipt(o));
//            b.append(s);
//            b.append(FileTool.getLineSeparator());
//        }
//        return b.toString();
//    }

//    @Deprecated
//    public void parseExternalId2OrderMap(String csv) {
//        String[] lines = csv.split(FileTool.getLineSeparator());
//        Deserializer<ReceiptMsg> deserializer = new Deserializer<ReceiptMsg>(ReceiptMsg.class);
//        for (String line : lines) {
//            String[] parts = line.split(";");
//            if (parts.length < 2) continue;
//            ReceiptMsg m = deserializer.convertFromString(parts[1]);
//            Receipt o = m.toReceipt();
//            externalId2Receipt.put(parts[0], ReceiptMsg.fromReceipt(o));
//        }
//    }

    public Id2ReceiptMap() {
        init();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    private void init() {
        externalId2Receipt =new ConcurrentHashMap<String, ReceiptMsg>();
        orderId2ExternalIdMap =new ConcurrentHashMap<String, String>();
    }

    private ConcurrentHashMap<String, ReceiptMsg> externalId2Receipt;
    private ConcurrentHashMap<String, String> orderId2ExternalIdMap;


} // class
