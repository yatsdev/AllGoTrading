package org.yats.trader.examples.server;

import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.connectivity.general.LastPriceServer;

import java.io.IOException;

public class LastPriceServerMain {

    public static void main(String args[])  {

        String configFilename = Tool.getPersonalConfigFilename("config/LastPriceServer");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
        LastPriceServer c = new LastPriceServer(prop);
        try {
            c.go();
        } catch (RuntimeException r)
        {
            r.printStackTrace();
        }

        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nexiting...\n");
        c.close();
        Tool.sleepFor(1000);

        System.exit(0);
    }

}
