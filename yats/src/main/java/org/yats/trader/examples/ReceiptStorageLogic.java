package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.messages.*;
import org.yats.trading.Receipt;
import org.yats.trading.ReceiptStorageCSV;

public class ReceiptStorageLogic implements IAmCalledBack{

    final Logger log = LoggerFactory.getLogger(ReceiptStorageLogic.class);

    @Override
    public void onCallback() {
        while(receiverReceipt.hasMoreMessages()) {
            ReceiptMsg m = receiverReceipt.get();
            Receipt r = m.toReceipt();
            log.info(r.toString());
            storage.onReceipt(r);
        }
    }

    public ReceiptStorageLogic() {
        storage = new ReceiptStorageCSV();
        Config config = Config.fromProperties(Config.createRealProperties());
        receiverReceipt = new BufferingReceiver<ReceiptMsg>(ReceiptMsg.class,
                config.getExchangeReceipts(),
                "#",
                config.getServerIP());
        receiverReceipt.setObserver(this);
        receiverReceipt.start();
    }

    private BufferingReceiver<ReceiptMsg> receiverReceipt;
    private ReceiptStorageCSV storage;

}
