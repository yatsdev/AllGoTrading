package org.yats.connectivity;

import org.yats.common.FileTool;
import org.yats.common.PropertiesReader;
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

    public String get(String orderId) {
        return orderId2ExternalIdMap.get(orderId);
    }

    public boolean containsReceiptForOrderId(String orderId) {
        if(!orderId2ExternalIdMap.containsKey(orderId)) return false;
        String externalId = orderId2ExternalIdMap.get(orderId);
        return externalId2Receipt.containsKey(externalId);
    }

    public void putReceipt(String externalId, Receipt receipt) {
        externalId2Receipt.put(externalId, receipt);
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

    public Id2ReceiptMap() {
        externalId2OrderMapFilename ="";
        orderId2ExternalIdMapFilename ="";
        externalId2Receipt =new ConcurrentHashMap<String, Receipt>();
        orderId2ExternalIdMap =new ConcurrentHashMap<String, String>();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////


    private void writeFileOandaId2OrderMap() {
        String csvString = toStringCSVExternalId2OrderMap();
        FileTool.writeToTextFile(externalId2OrderMapFilename, csvString, false);
    }

    private String toStringCSVExternalId2OrderMap() {
        StringBuilder b = new StringBuilder();
        for(String key : externalId2Receipt.keySet()) {
            Receipt o = externalId2Receipt.get(key);
            b.append(key); b.append(";");
            Serializer<ReceiptMsg> serializer = new Serializer<ReceiptMsg>();
            String s = serializer.convertToString(ReceiptMsg.fromReceipt(o));
            b.append(s);
            b.append(FileTool.getLineSeparator());
        }
        return b.toString();
    }

    private void writeFileOrderId2OandaIdMap() {
        String csvString = toStringCSVOrderId2ExternalIdMap();
        FileTool.writeToTextFile(orderId2ExternalIdMapFilename, csvString, false);
    }

    private void readFileOrderId2OandaIdMap() {
        String csvString = FileTool.exists(orderId2ExternalIdMapFilename)
                ? FileTool.readFromTextFile(orderId2ExternalIdMapFilename)
                : "";
        parseOrderId2ExternalIdMap(csvString);
    }

    private String toStringCSVOrderId2ExternalIdMap() {
        PropertiesReader r = PropertiesReader.createFromMap(orderId2ExternalIdMap);
        return r.toStringKeyValue();
    }

    private void readFileOandaId2OrderMap() {
        String csvString = FileTool.exists(externalId2OrderMapFilename)
                ? FileTool.readFromTextFile(externalId2OrderMapFilename)
                : "";
        parseExternalId2OrderMap(csvString);
    }

    private void parseExternalId2OrderMap(String csv) {
        String[] lines = csv.split(FileTool.getLineSeparator());
        Deserializer<ReceiptMsg> deserializer = new Deserializer<ReceiptMsg>(ReceiptMsg.class);
        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length < 2) continue;
            ReceiptMsg m = deserializer.convertFromString(parts[1]);
            Receipt o = m.toReceipt();
            externalId2Receipt.put(parts[0], o);
        }
    }

    private void parseOrderId2ExternalIdMap(String csv) {
        PropertiesReader r = PropertiesReader.createFromStringKeyValue(csv);
        ConcurrentHashMap<String, String> map = r.toMap();
        for(String key : map.keySet()) {
            orderId2ExternalIdMap.put(key, map.get(key));
        }
    }

    private ConcurrentHashMap<String, Receipt> externalId2Receipt;
    private ConcurrentHashMap<String, String> orderId2ExternalIdMap;
    private String orderId2ExternalIdMapFilename;
    private String externalId2OrderMapFilename;


} // class
