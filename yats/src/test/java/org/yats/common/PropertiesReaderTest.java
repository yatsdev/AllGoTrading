package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PropertiesReaderTest {


    @Test
    public void canReadBoolean()
    {
        assert (true == PropertiesReader.fromBooleanString("YES"));
        assert (true == PropertiesReader.fromBooleanString("Y"));
        assert (true == PropertiesReader.fromBooleanString("TRUE"));
        assert (true == PropertiesReader.fromBooleanString("True"));
        assert (true == PropertiesReader.fromBooleanString("1"));
        assert (false == PropertiesReader.fromBooleanString("0"));
        assert (false == PropertiesReader.fromBooleanString("dontknow"));
    }

    @BeforeMethod
    public void setUp() {
    }

}
