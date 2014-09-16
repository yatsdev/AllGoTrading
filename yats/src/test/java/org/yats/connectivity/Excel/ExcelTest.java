package org.yats.connectivity.Excel;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.IProvideProperties;
import org.yats.connectivity.excel.DDELinkEventListener;
import org.yats.connectivity.excel.SheetAccess;
import org.yats.connectivity.excel.IProvideDDEConversation;
import org.yats.connectivity.excel.MatrixItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExcelTest {


    @Test
    public void canConnectWithDDEToReportsSheetAndRequestFirstRow() {
        assert(4 == sheetAccessReports.countKeysInFirstRow());
    }

    @Test
    public void canConnectWithDDEToReportsSheetAndRequestFirstColumn() {
        assert(5 == sheetAccessReports.countKeysInFirstColumn());
    }

    private static String NA = "n/a";
    private static String TAB = "\t";
    private static String NL = "\r\n";

    @Test
    public void canParseSettingsFromSheet() {
        sheetAccessSettings.init(APPLICATION_NAME, SETTINGS_SHEET_NAME);
        sheetAccessSettings.readSettingsRows();
        Collection<IProvideProperties> sheet = sheetAccessSettings.parseSettingsRows();
        assert(sheet.size()==2);
        IProvideProperties first = (IProvideProperties) sheet.toArray()[0];
        if(first.get("strategyName").compareTo("testStrategy1")==0)
            assert(first.get("testParam1").compareTo("11.11")==0);
        else
            assert(first.get("testParam2").compareTo("22.22")==0);

        IProvideProperties second = (IProvideProperties) sheet.toArray()[1];
        if(second.get("strategyName").compareTo("testStrategy2")==0)
            assert(second.get("testParam2").compareTo("22.22")==0);
        else
            assert(second.get("testParam1").compareTo("11.11")==0);
    }

    @Test
    public void canPokeFirstRowOfMatrix() {
        List<MatrixItem> list = new ArrayList<MatrixItem>();
        String p1v1 = "17";
        String p1v2 = "18";
        String p1v4 = "20";
        list.add(new MatrixItem("p1", "importantParam1", p1v1));
        list.add(new MatrixItem("p1", "secondImportantParam", p1v2));
        list.add(new MatrixItem("p1", "numberOfActiveOrders", p1v4));
        sheetAccessReports.updateMatrix(list);

        assert(5 == sheetAccessReports.countKeysInFirstColumn());
        assert(4 == sheetAccessReports.countKeysInFirstRow());
        assert( mockToReports.getLastPokeLocation().compareTo("R2C2:R2C9")==0);
        String expectedRow1 = p1v1+TAB+NA+TAB+p1v2+TAB+NA+TAB+NA+TAB+NA+TAB+NA+TAB+p1v4+TAB;
        assert( mockToReports.getLastPokeString().compareTo(expectedRow1)==0);
    }

    @BeforeMethod
    public void setup()
    {
        mockToReports = new MockExcelLinkReports();
        sheetAccessReports = new SheetAccess(mockToReports);
        sheetAccessReports.connect(APPLICATION_NAME, REPORTS_SHEET_NAME);
        sheetAccessReports.readFirstRowFromDDE();
        sheetAccessReports.readFirstColumnFromDDE();
        mockToSettings = new MockExcelLinkSettings();
        sheetAccessSettings = new SheetAccess(mockToSettings);

    }

    private static final String REPORTS_SHEET_NAME = "..\\config\\[ExcelDemoWMacro.xlsm]Reports";
    private static final String SETTINGS_SHEET_NAME = "..\\config\\[ExcelDemoWMacro.xlsm]Settings";
    private static final String APPLICATION_NAME = "Excel";

    SheetAccess sheetAccessSettings;
    SheetAccess sheetAccessReports;
    MockExcelLinkReports mockToReports;
    MockExcelLinkSettings mockToSettings;


    public static class MockExcelLinkReports implements IProvideDDEConversation {

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


    public static class MockExcelLinkSettings implements IProvideDDEConversation {

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
                return TAB+"testParam1"+TAB+"testParam2"+NL;
            } else if(what.compareTo("C1")==0) {
                return TAB+"testStrategy1"+NL+NL+"testStrategy2"+NL;
            } else if(what.compareTo("R2")==0) {
                return "testStrategy1" +TAB+ "11.11"+NL;
            } else if(what.compareTo("R4")==0) {
                return "testStrategy2" +TAB+TAB+ "22.22"+NL;
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
