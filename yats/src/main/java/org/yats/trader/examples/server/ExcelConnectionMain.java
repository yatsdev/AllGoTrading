package org.yats.trader.examples.server;

import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.excel.DDELink;
import org.yats.connectivity.excel.ExcelConnection;
import org.yats.trading.ProductList;

import java.io.IOException;

public class ExcelConnectionMain {


    public static void main(String args[])  {

        final String className = ExcelConnectionMain.class.getSimpleName();
        String configFilename = Tool.getPersonalConfigFilename("config",className);
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
        ProductList products = ProductList.createFromFile("config/CFDProductList.csv");
        ExcelConnection c = new ExcelConnection(prop, products, new DDELink(), new DDELink(), new DDELink());
        try {
            c.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

} // class
