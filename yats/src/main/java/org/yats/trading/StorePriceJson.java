package org.yats.trading;

import org.yats.common.FileTool;
import org.yats.messagebus.Serializer;
import org.yats.messagebus.messages.PriceDataMsg;

public class StorePriceJson implements IStorePrice {

    @Override
    public void store(PriceData p) {
        PriceDataMsg m = PriceDataMsg.createFrom(p);
        Serializer<PriceDataMsg> serializer = new Serializer<PriceDataMsg>();
        String data = serializer.convertToString(m);
        FileTool.writeToTextFile(filename, data + "\n", true);
    }

    public StorePriceJson(String baseLocation, String productId) {
        this.productId = productId;
        FileTool.createDirectories(baseLocation);
        this.filename = baseLocation + "/" + productId + ReadPriceJSON.SUFFIX;

    }

    String filename;
    String productId;
}
