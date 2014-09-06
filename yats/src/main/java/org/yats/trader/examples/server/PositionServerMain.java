package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PositionRequestMsg;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.*;

public class PositionServerMain implements IAmCalledBack {


    public static void main(String args[]) throws Exception {

        try {
            final String className = PositionServerMain.class.getSimpleName();
            String configFilename = Tool.getPersonalConfigFilename("config/"+className);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
            PositionServerMain positionServerLogic = new PositionServerMain(prop);
            positionServerLogic.log.info("Starting "+className);

//        Config positionServerConfig =  FileTool.exists(pathToConfigFile)
//                ? Config.fromProperties(PropertiesReader.createFromConfigFile(pathToConfigFile))
//                : Config.fromProperties(Config.createRealProperties());

//        IProvideProperties p =FileTool.exists(pathToConfigFile)
//                ? PropertiesReader.createFromConfigFile(pathToConfigFile)
//                : Config.createRealProperties();

            IProvideProduct productList = ProductList.createFromFile("config/CFDProductList.csv");

            positionServerLogic.startRequestListener();
            PositionStorageCSV storage = new PositionStorageCSV(prop.get("positionFilename"));
            positionServerLogic.setPositionStorage(storage);
            positionServerLogic.setProductList(productList);

            Thread.sleep(2000);

            System.out.println("\n===");
            System.out.println("Initialization done.");
            System.out.println("Press enter to exit.");
            System.out.println("===\n");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            System.out.println("\nexiting...\n");

            Thread.sleep(1000);

            positionServerLogic.close();

            positionServerLogic.log.info("Done with "+className);
            System.exit(0);
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    final Logger log = LoggerFactory.getLogger(PositionServerMain.class);

    @Override
    public synchronized void onCallback() {
        while(receiverPositionRequests.hasMoreMessages()) {
            answerPositionRequest();
        }
        while(receiverPositionSnapshots.hasMoreMessages()) {
            receiveNewPositionSnapshot();
        }
        boolean gotNewReceipt=false;
        while(receiverReceipts.hasMoreMessages()) {
            gotNewReceipt=true;
            try {
                positionServer.onReceipt(receiverReceipts.get().toReceipt());
            } catch(TradingExceptions.UnknownIdException e) {
                log.error(e.getMessage());
            }
        }
        if(gotNewReceipt && config.isPublishAllPositionSnapshots()) {
            publishPositionSnapshot();
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
        publishPositionSnapshot();
    }

    private void publishPositionSnapshot() {
        PositionSnapshot snapshot = positionServer.getPositionSnapshot();
        PositionSnapshotMsg msg = PositionSnapshotMsg.fromPositionSnapshot(snapshot);
        log.debug("publishing snapshot:"+msg.toString());
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

    public PositionServerMain(IProvideProperties p) {
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

} // class
