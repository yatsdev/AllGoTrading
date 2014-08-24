package org.yats.trader.examples.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;

import java.io.IOException;

public class ReportGeneratorMain {

    final Logger log = LoggerFactory.getLogger(ReportGeneratorMain.class);

    public void go() throws InterruptedException, IOException
    {

        log.info("Starting ReportGeneratorMain...");
        String configFilename = Tool.getPersonalConfigFilename("config/ReportGenerator");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
        ReportGeneratorLogic logic = new ReportGeneratorLogic(prop);

        Thread.sleep(1000);

        logic.go();

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        logic.close();

        Thread.sleep(2500);



        log.info("ReportGeneratorMain done.");
        System.exit(0);
    }

    public ReportGeneratorMain() {
    }

    public static void main(String args[]) throws Exception {
        ReportGeneratorMain q = new ReportGeneratorMain();

        try {
            q.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

}
