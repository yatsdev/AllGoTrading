package org.yats.trading;

import org.joda.time.DateTime;
import org.yats.common.FileTool;
import org.yats.messagebus.Serializer;
import org.yats.messagebus.messages.PriceDataMsg;

public class StorePriceCSV implements IStorePrice {

    @Override
    public void store(PriceData p) {
        PriceDataMsg m = PriceDataMsg.createFrom(p);
        Serializer<PriceDataMsg> serializer = new Serializer<PriceDataMsg>();
        String data = serializer.convertToString(m);
        FileTool.writeToTextFile(filename, data + "\n", true);
    }

    public StorePriceCSV(String baseLocation, String productId) {
        this.productId = productId;
        FileTool.createDirectories(baseLocation);
        this.filename = baseLocation + "/" + productId + ".csv";

    }

    String filename;
    String productId;
}
