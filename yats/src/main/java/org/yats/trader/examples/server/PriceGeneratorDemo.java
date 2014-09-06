package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;

import java.io.IOException;

public class PriceGeneratorDemo {

    final Logger log = LoggerFactory.getLogger(ReceiptStorageMain.class);

    public void go() throws InterruptedException, IOException
    {

        log.info("Starting PriceGeneratorDemo...");
        String configFilename = Tool.getPersonalConfigFilename("config/PriceGeneratorDemo");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
        PriceGeneratorLogic logic = new PriceGeneratorLogic(prop);

        Thread.sleep(1000);

        logic.go();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        Thread.sleep(1000);

        logic.close();

        log.info("ReceiptStorageDemo done.");
        System.exit(0);
    }

    public PriceGeneratorDemo() {
    }

    public static void main(String args[]) throws Exception {
        PriceGeneratorDemo q = new PriceGeneratorDemo();

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
