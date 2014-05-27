package org.yats.trader.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PositionServerMain {


    final Logger log = LoggerFactory.getLogger(ReceiptStorageMain.class);

    public void go() throws InterruptedException, IOException
    {
        log.info("Starting PositionServerMain...");
        PositionServerLogic positionServerLogic = new PositionServerLogic();

        Thread.sleep(2000);

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        Thread.sleep(1000);

        log.info("PositionServerMain done.");
        System.exit(0);
    }

    public PositionServerMain() {
    }

    public static void main(String args[]) throws Exception {
        PositionServerMain q = new PositionServerMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

} // class
