package org.yats.messagebus;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yats.common.Decimal;
import org.yats.messagebus.messages.MarketDataMsg;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.trading.AccountPosition;
import org.yats.trading.MarketData;
import org.yats.trading.PositionSnapshot;


public class SenderReceiverTest {

    // can only be executed successfully if RabbitMQ server ip is set correctly
//    private static final String CATCH_ALL_TOPIC = "#";


    @Test
    public void canSendAndReceiveMarketData()
    {
        senderMarketData.publish(dataMsg.getTopic(), dataMsg);
        MarketDataMsg newDataMsg = receiverMarketData.tryReceive(1000);
        MarketData newData = newDataMsg.toMarketData();

        assert (newDataMsg!=null);
        assert (newDataMsg.isSameAs(dataMsg));
        assert (newData.isSameAs(data));
    }

    @Test
    public void canSendAndReceivePositionSnapshot()
    {
        PositionSnapshotMsg positionSnapshotMsg = PositionSnapshotMsg.fromPositionSnapshot(positionSnapshot);
        senderPositionSnapshot.publish(positionSnapshotMsg.getTopic(), positionSnapshotMsg);
        PositionSnapshotMsg data = receiverPositionSnapshot.tryReceive(1000);
        PositionSnapshot newPositionSnapshot = data.toPositionSnapshot();

        assert (newPositionSnapshot!=null);
        assert (data.isSameAs(positionSnapshotMsg));
        assert (newPositionSnapshot.isSameAs(positionSnapshot));
    }


    @BeforeMethod
    public void setUp() {
        Config config = Config.DEFAULT_FOR_TESTS;
        senderMarketData = new Sender<MarketDataMsg>(config.getExchangeMarketData(),config.getServerIP());
        senderMarketData.init();
        receiverMarketData = new Receiver<MarketDataMsg>(
                MarketDataMsg.class,
                config.getExchangeMarketData(),
                config.getTopicCatchAll(),
                config.getServerIP());
        data = new MarketData(new DateTime(DateTimeZone.UTC), "test",
                Decimal.fromDouble(11), Decimal.fromDouble(12), Decimal.fromDouble(12),
                Decimal.fromDouble(20), Decimal.fromDouble(30), Decimal.ONE );
        dataMsg = MarketDataMsg.createFrom(data);

        senderPositionSnapshot = new Sender<PositionSnapshotMsg>(config.getExchangePositionSnapshot(),config.getServerIP());
        senderPositionSnapshot.init();
        receiverPositionSnapshot = new Receiver<PositionSnapshotMsg>(
                PositionSnapshotMsg.class,
                config.getExchangePositionSnapshot(),
                config.getTopicCatchAll(),
                config.getServerIP()
        );
        positionSnapshot = new PositionSnapshot();
        positionSnapshot.add(new AccountPosition("product1", "account1", Decimal.fromDouble(11)));
        positionSnapshot.add(new AccountPosition("product2", "account1", Decimal.fromDouble(12)));
        positionSnapshot.add(new AccountPosition("product1", "account2", Decimal.fromDouble(13)));
    }


    private Sender<MarketDataMsg> senderMarketData;
    private Receiver<MarketDataMsg> receiverMarketData;
    private MarketData data;
    private MarketDataMsg dataMsg;

    private PositionSnapshot positionSnapshot;
    private Sender<PositionSnapshotMsg> senderPositionSnapshot;
    private Receiver<PositionSnapshotMsg> receiverPositionSnapshot;


} // class
