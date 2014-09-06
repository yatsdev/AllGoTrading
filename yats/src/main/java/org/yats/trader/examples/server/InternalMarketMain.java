package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.matching.InternalMarketRunner;

import java.io.IOException;

public class InternalMarketMain {

    final Logger log = LoggerFactory.getLogger(InternalMarketMain.class);

    public void go() throws InterruptedException, IOException
    {

        log.info("Starting InternalMarketMain...");
        String configFilename = Tool.getPersonalConfigFilename("config/InternalMarketMain");
        IProvideProperties prop = PropertiesReader.createFromConfigFile(configFilename);
        InternalMarketRunner logic = new InternalMarketRunner(prop);

        Thread.sleep(500);

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();
        System.out.println("\nexiting...\n");

        logic.shutdown();

        Thread.sleep(1000);

        log.info("ReceiptStorageMain done.");
        System.exit(0);
    }

    public InternalMarketMain() {
    }

    public static void main(String args[]) throws Exception {
        InternalMarketMain q = new InternalMarketMain();

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
