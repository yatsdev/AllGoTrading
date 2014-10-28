package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileToolTest {

    @Test(groups = { "integration", "inMemory" })
    public void canGetLineSeparator() {
        String newline = FileTool.getLineSeparator();
        if(Tool.isWindows())
            assert(newline.equals("\r\n"));
        else
            assert(newline.equals("\n"));
    }


    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setup() {

    }


} // class
