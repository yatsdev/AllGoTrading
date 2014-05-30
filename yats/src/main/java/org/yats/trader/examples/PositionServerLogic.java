package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.trading.PositionRequest;
import org.yats.trading.PositionServer;
import org.yats.trading.PositionSnapshot;

public class PositionServerLogic implements IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(PositionServerLogic.class);

    @Override
    public void onCallback() {
        while(receiverPositionRequests.hasMoreMessages()) {
            if(positionServer.isEmpty()) continue;
            answerPositionRequest();
        }
        while(receiverPositionSnapshots.hasMoreMessages()) {
            acceptNewPositionSnapshot();
        }
    }

    private void acceptNewPositionSnapshot() {
        PositionSnapshotMsg m = receiverPositionSnapshots.get();
        PositionSnapshot s = m.toPositionSnapshot();
        log.debug("acceptNewPositionSnapshot received:" + m.toString());
        positionServer.setPositionSnapshot(s);
    }

    private void answerPositionRequest() {
        receiverPositionRequests.get();
        PositionSnapshot snapshot = positionServer.getPositionSnapshot();
        PositionSnapshotMsg msg = PositionSnapshotMsg.fromPositionSnapshot(snapshot);
        log.debug("answerPositionRequest publishing:"+msg.toString());
        senderPositionSnapshot.publish(config.getTopicPositionSnapshot(), msg);
    }

    public PositionServerLogic(Config config) {
        this.config=config;
        positionServer = new PositionServer();
        receiverPositionRequests = new BufferingReceiver<PositionRequestMsg>(PositionRequestMsg.class,
                config.getExchangePositionRequest(),
                "#",
                config.getServerIP());
        receiverPositionRequests.setObserver(this);

        receiverPositionSnapshots= new BufferingReceiver<PositionSnapshotMsg>(PositionSnapshotMsg.class,
                config.getExchangePositionSnapshot(),
                "#",
                config.getServerIP());
        receiverPositionSnapshots.setObserver(this);

        senderPositionSnapshot = new Sender<PositionSnapshotMsg>(
                config.getExchangePositionSnapshot(),
                config.getServerIP());

        senderPositionRequest = new Sender<PositionRequestMsg>(
                config.getExchangePositionRequest(),
                config.getServerIP());
    }

    public void startRequestListener()
    {
        receiverPositionRequests.start();
    }

    public void startSnapshotListener()
    {
        receiverPositionSnapshots.start();
    }

    public void setPositionServer(PositionServer positionServer) {

        this.positionServer = positionServer;
    }

    private BufferingReceiver<PositionRequestMsg> receiverPositionRequests;
    private BufferingReceiver<PositionSnapshotMsg> receiverPositionSnapshots;
    private Sender<PositionSnapshotMsg> senderPositionSnapshot;
    private Sender<PositionRequestMsg> senderPositionRequest;
    private PositionServer positionServer;
    private Config config;

    public void requestPositionSnapshotFromPositionServer() {
        PositionRequest pr = new PositionRequest("*","*");
        senderPositionRequest.publish(PositionRequestMsg.POSITIONREQUEST_TOPIC,
                PositionRequestMsg.fromPositionRequest(pr));
    }

} // class
