package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.connectivity.fix.OrderConnection;
import org.yats.connectivity.fix.PriceFeed;
import org.yats.trader.StrategyRunner;
import org.yats.trading.ProductList;

import java.io.IOException;

public class ReceiptStorageMain {

    final Logger log = LoggerFactory.getLogger(ReceiptStorageMain.class);

    public void go() throws InterruptedException, IOException
    {
        log.info("Starting ReceiptStorageMain...");
        ReceiptStorageLogic storage = new ReceiptStorageLogic();

        Thread.sleep(2000);

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        Thread.sleep(1000);

        log.info("ReceiptStorageMain done.");
        System.exit(0);
    }

    public ReceiptStorageMain() {
    }

    public static void main(String args[]) throws Exception {
        ReceiptStorageMain q = new ReceiptStorageMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            System.exit(-1);
        }
        System.exit(0);

    }

}
