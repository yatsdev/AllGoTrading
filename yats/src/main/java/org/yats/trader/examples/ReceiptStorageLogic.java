package org.yats.trader.examples;

import org.yats.common.IAmCalledBack;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.messages.*;
import org.yats.trading.Receipt;
import org.yats.trading.ReceiptStorageCSV;

public class ReceiptStorageLogic implements IAmCalledBack{


    @Override
    public void onCallback() {
        while(receiverReceipt.hasMoreMessages()) {
            ReceiptMsg m = receiverReceipt.get();
            Receipt r = m.toReceipt();
            storage.onReceipt(r);
        }
    }

    public ReceiptStorageLogic() {
        storage = new ReceiptStorageCSV();
        receiverReceipt = new BufferingReceiver<ReceiptMsg>(ReceiptMsg.class,
                Config.EXCHANGE_NAME_FOR_RECEIPTS_DEFAULT,
                "#",
                Config.SERVER_IP_DEFAULT);
        receiverReceipt.setObserver(this);
        receiverReceipt.start();
    }

    private BufferingReceiver<ReceiptMsg> receiverReceipt;
    private ReceiptStorageCSV storage;

}
