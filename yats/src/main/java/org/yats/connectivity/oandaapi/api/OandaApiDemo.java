package org.yats.connectivity.oandaapi.api;

import org.yats.common.Decimal;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.trading.*;

public class OandaApiDemo implements IConsumeReceipt {

    private boolean gotit=false;

    @Override
    public void onReceipt(Receipt receipt) {
        gotit=true;
    }


    public void go() {
        String configFilename = Tool.getPersonalConfigFilename("config/OandaApi");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        OandaApi oandaApi = new OandaApi(prop);
        oandaApi.setProductProvider(products);
        oandaApi.setReceiptConsumer(this);

        oandaApi.logon();


        OrderNew order = OrderNew.create()
                .withBookSide(BookSide.BID)
                .withInternalAccount("demo")
                .withProductId("OANDA_EURUSD")
                .withLimit(Decimal.fromString("1.337"))
                .withSize(Decimal.HUNDRED)
                ;
        oandaApi.sendOrderNew(order);

        while(!gotit) Tool.sleepABit();

        oandaApi.shutdown();
    }

    public static void main(String args[]) throws Exception
    {
        OandaApiDemo d = new OandaApiDemo();
        d.go();
    }

}
