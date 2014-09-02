package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.CommonExceptions;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.*;

public class PositionServerLogic implements IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(PositionServerLogic.class);

    @Override
    public void onCallback() {
        while(receiverPositionRequests.hasMoreMessages()) {
            answerPositionRequest();
        }
        while(receiverPositionSnapshots.hasMoreMessages()) {
            receiveNewPositionSnapshot();
        }
        while(receiverReceipts.hasMoreMessages()) {
            try {
                positionServer.onReceipt(receiverReceipts.get().toReceipt());
            } catch(TradingExceptions.UnknownIdException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void receiveNewPositionSnapshot() {
        PositionSnapshotMsg p = receiverPositionSnapshots.get();
        if(gotSnapshotOnce) return;
        gotSnapshotOnce=true;
        PositionSnapshot s = p.toPositionSnapshot();
        log.debug("receiveNewPositionSnapshot received:" + p.toString());
        positionServer.setPositionSnapshot(s);
        subscribeAllProductsOf(s);
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

    public void close() {
        receiverPositionRequests.close();
        receiverPositionSnapshots.close();
        receiverReceipts.close();
        senderPositionSnapshot.close();
        senderPositionRequest.close();
        senderSubscription.close();
    }

    public PositionServerLogic(IProvideProperties p) {
        gotSnapshotOnce=false;
        this.config=Config.fromProperties(p);
        positionServer = new PositionServer();
        receiverPositionRequests = new BufferingReceiver<PositionRequestMsg>(PositionRequestMsg.class,
                config.getExchangePositionRequest(),
                "#",
                config.getServerIP());
        receiverPositionRequests.setObserver(this);
        //receiverPositionRequests.start(); // started in separate method

        receiverPositionSnapshots= new BufferingReceiver<PositionSnapshotMsg>(PositionSnapshotMsg.class,
                config.getExchangePositionSnapshot(),
                "#",
                config.getServerIP());
        receiverPositionSnapshots.setObserver(this);
        //receiverPositionSnapshots.start();  // started in separate method

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

        senderPositionRequest = new Sender<PositionRequestMsg>(config.getExchangePositionRequest(),config.getServerIP());
        senderSubscription = new Sender<SubscriptionMsg>(config.getExchangeSubscription(), config.getServerIP());
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

    public void setProductList(IProvideProduct productList) {
        positionServer.setProductList(productList);
    }

    public void setPositionStorage(IStorePositionSnapshots positionStorage) {
        positionServer.setPositionStorage(positionStorage);
        if(config.isStorePositionsToDisk()) {
            positionServer.initFromLastStoredPositionSnapshot();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private void subscribeAllProductsOf(PositionSnapshot s) {
        for(AccountPosition a : s.getAllPositions()) {
            SubscriptionMsg m = SubscriptionMsg.fromProductId(a.getProductId());
            senderSubscription.publish(config.getTopicSubscriptions(), m);
        }
    }

    private BufferingReceiver<ReceiptMsg> receiverReceipts;
    private BufferingReceiver<PositionRequestMsg> receiverPositionRequests;
    private BufferingReceiver<PositionSnapshotMsg> receiverPositionSnapshots;
    private Sender<PositionSnapshotMsg> senderPositionSnapshot;
    private Sender<PositionRequestMsg> senderPositionRequest;
    Sender<SubscriptionMsg> senderSubscription;
    private PositionServer positionServer;
    private Config config;
    private boolean gotSnapshotOnce;


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
