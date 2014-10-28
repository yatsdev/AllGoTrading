package org.yats.messagebus;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.messagebus.messages.KeyValueMsg;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.trading.*;

public class MessageConversionTest {

        @Test(groups = { "integration", "inMemory" })
        public void canConvertPositionRequest()
        {
            PositionRequestMsg m = PositionRequestMsg.fromPositionRequest(positionRequest);
            PositionRequest newPositionRequest = m.toPositionRequest();
            PositionRequestMsg m2 = PositionRequestMsg.fromPositionRequest(newPositionRequest);
            assert (m.toString().length()!=0);
            assert (m.toString().compareTo(m2.toString())==0);
        }

        @Test(groups = { "integration", "inMemory" })
        public void canConvertKeyValueMessage()
        {
            KeyValueMsg m = new KeyValueMsg();
            m.message = "";
            assert(m.toProperties().getKeySet().size()==0);
            m.message = ";";
            assert(m.toProperties().getKeySet().size()==0);
            m.message = "a=15";
            assert(m.toProperties().getKeySet().size()==1);
            m.message = "a=15;b=33.2;";
            assert(m.toProperties().getKeySet().size()==2);
            m.message = "a=15;b=33.2;c=";
            assert(m.toProperties().getKeySet().size()==3);
            assert(m.toProperties().get("a").compareTo("15")==0);
            assert(m.toProperties().get("c").length()==0);
        }


        @BeforeMethod(groups = { "integration", "inMemory" })
        public void setUp() {
            positionRequest = new PositionRequest(ProductTest.ACCOUNT1, ProductTest.TEST_PRODUCT1_ID);

        }

        PositionRequest positionRequest;
        KeyValueMsg keyValueMessage;

    } // class
