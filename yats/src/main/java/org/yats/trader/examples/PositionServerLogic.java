package org.yats.trader.examples;

import org.yats.common.IAmCalledBack;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.PositionRequest;
import org.yats.trading.PositionServer;

public class PositionServerLogic implements IAmCalledBack {

        @Override
        public void onCallback() {
        while(receiverPositionRequests.hasMoreMessages()) {
            SubscriptionMsg m = receiverPositionRequests.get();
            positionServer.getAccountPosition(new PositionRequest("",""));
        }
        }


        public PositionServerLogic() {
        positionServer = new PositionServer();
        receiverPositionRequests = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                Config.EXCHANGE_NAME_FOR_SUBSCRIPTIONS_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
        receiverPositionRequests.setObserver(this);
        receiverPositionRequests.start();
            }

        private BufferingReceiver<SubscriptionMsg> receiverPositionRequests;
        private PositionServer positionServer;

} // class
