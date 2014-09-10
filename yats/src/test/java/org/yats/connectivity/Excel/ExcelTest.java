package org.yats.connectivity.Excel;


import org.testng.annotations.Test;
import org.yats.connectivity.excel.ExcelTools;

import java.util.Vector;

public class ExcelTest {

    @Test
    public void canConvertR1StringtoVector()
    {
        Vector<String> R1=new Vector<String>();
        String R1request=new String("\tlastStep\taverageOrderSize\r\n");//This will be improved later on by actually requesting the R1 column in the test Excel file
        ExcelTools test=new ExcelTools();
        R1.add("lastStep");
        R1.add("averageOrderSize");
        assert(test.R1ColumnParser(R1request).equals(R1));

    }






}
