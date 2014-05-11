package org.yats.messagebus;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.messagebus.messages.MarketDataMsg;
import org.yats.trading.MarketData;



public class SenderReceiverTest {

    // can only be executed successfully if RabbitMQ server ip is set correctly
    private static String EXCHANGE = "exchangeSenderReceiverTest";
    private static final String TOPIC_FOR_ALL_TEST_MarketDataMsg = MarketDataMsg.class.getSimpleName()+".#";
    private static String SERVERIP = "127.0.0.1";


    @Test
    public void canSendAndReceive()
    {
        sender.publish(dataMsg.getTopic(),dataMsg);
        MarketDataMsg newDataMsg = receiver.tryReceive(1000);
        MarketData newData = newDataMsg.toMarketData();

        assert (newDataMsg!=null);
        assert (newDataMsg.isSameAs(dataMsg));
        assert (newData.isSameAs(data));
    }


    @BeforeMethod
    public void setUp() {
        sender = new Sender<MarketDataMsg>(EXCHANGE,SERVERIP);
        sender.init();
        receiver = new Receiver<MarketDataMsg>(MarketDataMsg.class, EXCHANGE,
                TOPIC_FOR_ALL_TEST_MarketDataMsg, SERVERIP);
        data = new MarketData(new DateTime(DateTimeZone.UTC), "test", Decimal.createFromDouble(11),
                Decimal.createFromDouble(12), Decimal.createFromDouble(20), Decimal.createFromDouble(30) );
        dataMsg = MarketDataMsg.createFrom(data);
    }


    private Sender<MarketDataMsg> sender;
    private Receiver<MarketDataMsg> receiver;
    private MarketData data;
    private MarketDataMsg dataMsg;

} // class
