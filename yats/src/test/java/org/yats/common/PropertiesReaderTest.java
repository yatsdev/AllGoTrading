package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PropertiesReaderTest {


    @Test(groups = { "integration", "inMemory" })
    public void canReadBoolean()
    {
        assert (PropertiesReader.fromBooleanString("YES"));
        assert (PropertiesReader.fromBooleanString("Y"));
        assert (PropertiesReader.fromBooleanString("TRUE"));
        assert (PropertiesReader.fromBooleanString("True"));
        assert (PropertiesReader.fromBooleanString("1"));
        assert (!PropertiesReader.fromBooleanString("0"));
        assert (!PropertiesReader.fromBooleanString("dontknow"));
    }

    @BeforeMethod(groups = { "integration", "inMemory" })
    public void setUp() {
    }

}
