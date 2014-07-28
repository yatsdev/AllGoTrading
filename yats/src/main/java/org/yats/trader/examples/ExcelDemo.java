package org.yats.trader.examples;

import org.yats.common.Tool;
import org.yats.connectivity.excel.ExcelConnection;

public class ExcelDemo {

        public static void main(String[] args){
            ExcelConnection connection=new ExcelConnection("AllGoTrading.xlsx");
            
            Tool.sleepFor(30000);
//            connection.startExcelLink();
        }

} // class
