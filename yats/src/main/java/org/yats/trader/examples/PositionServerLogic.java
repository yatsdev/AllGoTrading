package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.common.IAmCalledBack;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trading.*;

public class PositionServerLogic implements IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(PositionServerLogic.class);

    @Override
    public void onCallback() {
        while(receiverPositionRequests.hasMoreMessages()) {
            if(positionServer.isEmpty()) continue;
            answerPositionRequest();
        }
        while(receiverPositionSnapshots.hasMoreMessages()) {
            receiveNewPositionSnapshot();
        }
        while(receiverReceipts.hasMoreMessages()) {
            positionServer.onReceipt(receiverReceipts.get().toReceipt());
        }
    }

    private void receiveNewPositionSnapshot() {
        PositionSnapshotMsg m = receiverPositionSnapshots.get();
        PositionSnapshot s = m.toPositionSnapshot();
        log.debug("receiveNewPositionSnapshot received:" + m.toString());
        positionServer.setPositionSnapshot(s);
    }

    private void answerPositionRequest() {
        receiverPositionRequests.get();
        PositionSnapshot snapshot = positionServer.getPositionSnapshot();
        PositionSnapshotMsg msg = PositionSnapshotMsg.fromPositionSnapshot(snapshot);
        log.debug("answerPositionRequest publishing:"+msg.toString());
        senderPositionSnapshot.publish(config.getTopicPositionSnapshot(), msg);
    }

    public void requestPositionSnapshotFromPositionServer() {
        PositionRequest pr = new PositionRequest("*","*");
        senderPositionRequest.publish(PositionRequestMsg.POSITIONREQUEST_TOPIC,
                PositionRequestMsg.fromPositionRequest(pr));
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

        receiverReceipts = new BufferingReceiver<ReceiptMsg>(ReceiptMsg.class,
                config.getExchangeReceipts(),
                "#",
                config.getServerIP());
        if(config.isListeningForReceipts()) {
            receiverReceipts.setObserver(this);
            receiverReceipts.start();
        }

        senderPositionSnapshot = new Sender<PositionSnapshotMsg>(
                config.getExchangePositionSnapshot(),
                config.getServerIP());

        senderPositionRequest = new Sender<PositionRequestMsg>(
                config.getExchangePositionRequest(),
                config.getServerIP());
    }

    public void startRequestListener()   // todo: make setting
    {
        receiverPositionRequests.start();
    }

    public void startSnapshotListener()  // todo: make setting
    {
        receiverPositionSnapshots.start();
    }

    public void setPositionServer(PositionServer positionServer) {
        this.positionServer = positionServer;
    }

    public void setPositionStorage(IStorePositionSnapshots positionStorage) {
        positionServer.setPositionStorage(positionStorage);
    }

    private BufferingReceiver<ReceiptMsg> receiverReceipts;
    private BufferingReceiver<PositionRequestMsg> receiverPositionRequests;
    private BufferingReceiver<PositionSnapshotMsg> receiverPositionSnapshots;
    private Sender<PositionSnapshotMsg> senderPositionSnapshot;
    private Sender<PositionRequestMsg> senderPositionRequest;
    private PositionServer positionServer;
    private Config config;



    private class PositionStorageDummy implements IStorePositionSnapshots {
        @Override
        public void store(PositionSnapshot positionSnapshot) {
            throw new CommonExceptions.DummyException("This is PositionStorageDummy.");
        }

        @Override
        public PositionSnapshot readLast() {
            throw new CommonExceptions.DummyException("This is PositionStorageDummy.");
        }
    }
} // class
