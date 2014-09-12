package org.yats.connectivity.Excel;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.connectivity.excel.DDELinkEventListener;
import org.yats.connectivity.excel.ExcelTools;
import org.yats.connectivity.excel.IProvideDDEConversation;
import org.yats.connectivity.excel.MatrixItem;

import java.util.ArrayList;
import java.util.List;

public class ExcelTest {


    @Test
    public void canConnectWithDDEToReportsSheetAndRequestFirstRow() {
        assert(4 == excelTools.countKeysInFirstRow());
    }

    @Test
    public void canConnectWithDDEToReportsSheetAndRequestFirstColumn() {
        assert(5 == excelTools.countKeysInFirstColumn());
    }

    private static String NA = "n/a";
    private static String TAB = "\t";
    private static String NL = "\r\n";

    @Test
    public void canPokeFirstRowOfMatrix() {
        List<MatrixItem> list = new ArrayList<MatrixItem>();
        String p1v1 = "17";
        String p1v2 = "18";
        String p1v4 = "20";
        list.add(new MatrixItem("p1", "importantParam1", p1v1));
        list.add(new MatrixItem("p1", "secondImportantParam", p1v2));
        list.add(new MatrixItem("p1", "numberOfActiveOrders", p1v4));
        excelTools.updateMatrix(list);

        assert(5 == excelTools.countKeysInFirstColumn());
        assert(4 == excelTools.countKeysInFirstRow());
        assert( mockToReports.getLastPokeLocation().compareTo("R2C2:R2C9")==0);
        String expectedRow1 = p1v1+TAB+NA+TAB+p1v2+TAB+NA+TAB+NA+TAB+NA+TAB+NA+TAB+p1v4+TAB;
        assert( mockToReports.getLastPokeString().compareTo(expectedRow1)==0);
    }

    @BeforeMethod
    public void setup()
    {
        mockToReports = new ReportsExcelLinkMock();
        excelTools = new ExcelTools(mockToReports);
        excelTools.connect(APLICATION_NAME, REPORTS_SHEET_NAME);
        excelTools.readFirstRowFromDDE();
        excelTools.readFirstColumnFromDDE();
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
            lastPokeLocation=where;
            lastPokeString=what;
        }

        @Override
        public String request(String what) {

            if(what.compareTo("R1")==0) {
                return "\timportantParam1\t\tsecondImportantParam\t\t\tsomethingElse\t\tnumberOfActiveOrders";
            } else if(what.compareTo("C1")==0) {
                return NL+"p1"+NL+NL+"p2"+NL+"p3"+NL+NL+NL+"p4"+NL+"somethingWithALongName,s p a c e s AndEvenADot.";
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

        public String getLastPokeLocation() {
            return lastPokeLocation;
        }

        public String getLastPokeString() {
            return lastPokeString;
        }

        private String lastPokeLocation="";
        private String lastPokeString="";
    }






}
