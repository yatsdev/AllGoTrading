package org.yats.trader.examples;

import org.yats.common.Tool;
import org.yats.connectivity.excel.ExcelConnection;

import java.io.IOException;

public class ExcelDemo {

    public static void main(String args[])  {
        ExcelConnection c = new ExcelConnection("config/ExcelDemo.xlsx");
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
