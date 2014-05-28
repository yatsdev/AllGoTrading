package org.yats.messagebus;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.trading.*;

public class MessageConversionTest {

        @Test
        public void canConvertPositionRequest()
        {
            PositionRequestMsg m = PositionRequestMsg.fromPositionRequest(positionRequest);
            PositionRequest newPositionRequest = m.toPositionRequest();
            PositionRequestMsg m2 = PositionRequestMsg.fromPositionRequest(newPositionRequest);
            assert (m.toString().length()!=0);
            assert (m.toString().compareTo(m2.toString())==0);
        }


        @BeforeMethod
        public void setUp() {
            positionRequest = new PositionRequest(ProductTest.ACCOUNT1, ProductTest.PRODUCT1.getProductId());
        }

        PositionRequest positionRequest;
    } // class
