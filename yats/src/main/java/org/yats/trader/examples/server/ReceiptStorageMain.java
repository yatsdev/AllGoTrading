package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IAmCalledBack;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.messagebus.BufferingReceiver;
import org.yats.messagebus.Config;
import org.yats.messagebus.messages.ReceiptMsg;
import org.yats.trading.Receipt;
import org.yats.trading.ReceiptStorageCSV;


public class ReceiptStorageMain implements IAmCalledBack {

    public static void main(String args[]) throws Exception {

        try {
            final String className = ReceiptStorageMain.class.getSimpleName();
            String configFilename = Tool.getPersonalConfigFilename("config",className);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
            ReceiptStorageMain storage = new ReceiptStorageMain(prop);
            storage.log.info("Starting"+className);

            Thread.sleep(2000);

            System.out.println("\n===");
            System.out.println("Initialization done.");
            System.out.println("Press enter to exit.");
            System.out.println("===\n");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            System.out.println("\nexiting...\n");

            storage.close();

            Thread.sleep(1000);

            storage.log.info("Done with "+className);
            System.exit(0);
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////


    final Logger log = LoggerFactory.getLogger(ReceiptStorageMain.class);

    @Override
    public synchronized void onCallback() {
        while(receiverReceipt.hasMoreMessages()) {
            ReceiptMsg m = receiverReceipt.get();
            Receipt r = m.toReceipt();
            log.info(r.toString());
            storage.onReceipt(r);
        }
    }

    public void close() {
        receiverReceipt.close();
    }

    public ReceiptStorageMain(IProvideProperties _prop) {
        prop = _prop;
        storage = new ReceiptStorageCSV(prop);

        Config config = Config.fromProperties(prop);
        receiverReceipt = new BufferingReceiver<ReceiptMsg>(ReceiptMsg.class,
                config.getExchangeReceipts(),
                "#",
                config.getServerIP());
        receiverReceipt.setObserver(this);
        receiverReceipt.start();
    }

    private IProvideProperties prop;
    private BufferingReceiver<ReceiptMsg> receiverReceipt;
    private ReceiptStorageCSV storage;




}
