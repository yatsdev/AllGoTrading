package org.yats.connectivity.Excel;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.connectivity.excel.DDELinkEventListener;
import org.yats.connectivity.excel.ExcelTools;
import org.yats.connectivity.excel.IProvideDDEConversation;

import java.util.Vector;

public class ExcelTest {

    /*
    Please leave Excel out of tests. Use mock objects to mimic the external connection.
    @Test
    public void canConvertR1StringtoVector()
    {
        Vector<String> R1=new Vector<String>();
        String R1request=new String("\tlastStep\taverageOrderSize\r\n");//This will be improved later on by actually requesting the R1 column in the test Excel file
        ExcelTools test=new ExcelTools(null);
        R1.add("lastStep");
        R1.add("averageOrderSize");
        assert(test.R1ColumnParser(R1request).equals(R1));

    }
    */

    @Test
    public void canConnectWithDDEToReportsSheetAndRequestFirstRow() {
        excelTools.connect(APLICATION_NAME, REPORTS_SHEET_NAME);
        excelTools.readFirstRowFromDDE();
        assert(4 == excelTools.countKeysInFirstRow());
    }

    @BeforeMethod
    public void setup()
    {
        mockToReports = new ReportsExcelLinkMock();
        excelTools = new ExcelTools(mockToReports);
    }

    private static final String REPORTS_SHEET_NAME = "..\\config\\[ExcelDemoWMacro.xlsm]Reports";
    private static final String APLICATION_NAME = "Excel";

    ExcelTools excelTools;
    ReportsExcelLinkMock mockToReports;


    public static class ReportsExcelLinkMock implements IProvideDDEConversation {
        @Override
        public void disconnect() {

        }

        @Override
        public void stopAdvice(String s) {

        }

        @Override
        public void poke(String where, String what) {

        }

        @Override
        public String request(String what) {
            if(what.compareTo("R1")==0) {
                return "\timportantParam1\t\tsecondImportantParam\t\t\tsomethingElse\t\tnumberOfActiveOrders";
            }
            return "";
        }

        @Override
        public void startAdvice(String s) {

        }

        @Override
        public void setEventListener(DDELinkEventListener listener) {

        }

        @Override
        public void setTimeout(int millis) {

        }

        @Override
        public void connect(String where, String what) {

        }
    }






}
