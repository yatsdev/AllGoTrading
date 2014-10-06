package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileToolTest {

    @Test
    public void canGetLineSeparator() {
        String newline = FileTool.getLineSeparator();
        assert(newline.equals("\n"));
    }


    @BeforeMethod
    public void setup() {

    }


} // class
