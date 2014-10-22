package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.messagebus.Deserializer;
import org.yats.messagebus.messages.PriceDataMsg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadPriceJSON implements IReadPrice {

    final Logger log = LoggerFactory.getLogger(ReadPriceJSON.class);
    public static final String SUFFIX = ".json";

    @Override
    public PriceData read(){
        if(reader==null) createReader();
        try {
            PriceData price = readAndParse();
            if(price==PriceData.NULL) close();
            return price;
        } catch (Exception e) {
            return PriceData.NULL;
        }
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ReadPriceJSON(String baseLocation, String productId){
        this.path = baseLocation + "/" + productId + SUFFIX;
        reader=null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private PriceData readAndParse() throws Exception {
        String nextLine;
        nextLine = reader.readLine();
        if(nextLine==null) return PriceData.NULL;
        Deserializer<PriceDataMsg> deserializer = new Deserializer<PriceDataMsg>(PriceDataMsg.class);
        PriceDataMsg m = deserializer.convertFromString(nextLine);
        return m.toPriceData();
    }

    private void createReader() {
        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonExceptions.FileReadException(e.getMessage());
        }
    }

    private String path;
    private BufferedReader reader;
}
