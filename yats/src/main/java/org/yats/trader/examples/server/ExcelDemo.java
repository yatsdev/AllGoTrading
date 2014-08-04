package org.yats.trader.examples.server;

import org.yats.connectivity.excel.ExcelConnection;
import org.yats.messagebus.Config;

import java.io.IOException;

public class ExcelDemo {

    public static void main(String args[])  {
        ExcelConnection c = new ExcelConnection(Config.createRealProperties());
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
