package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.FileTool;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.messagebus.Config;
import org.yats.trading.PositionStorageCSV;

import java.io.IOException;

public class PositionServerMain {


    final Logger log = LoggerFactory.getLogger(ReceiptStorageMain.class);

    public void go() throws InterruptedException, IOException
    {
        log.info("Starting PositionServerMain...");
        String pathToConfigFile = "config/PositionServer.properties";
//        Config positionServerConfig =  FileTool.exists(pathToConfigFile)
//                ? Config.fromProperties(PropertiesReader.createFromConfigFile(pathToConfigFile))
//                : Config.fromProperties(Config.createRealProperties());

        IProvideProperties p =FileTool.exists(pathToConfigFile)
                ? PropertiesReader.createFromConfigFile(pathToConfigFile)
                : Config.createRealProperties();
        PositionServerLogic positionServerLogic = new PositionServerLogic(p);
        positionServerLogic.startRequestListener();
        PositionStorageCSV storage = new PositionStorageCSV(p.get("positionFilename"));
        positionServerLogic.setPositionStorage(storage);

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
