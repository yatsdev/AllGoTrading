package org.yats.trader.examples.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.PositionSnapshotMsg;
import org.yats.trading.AccountPosition;
import org.yats.trading.PositionSnapshot;
import org.yats.trading.ProductList;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PositionGeneratorTool implements Runnable {

    public static void main(String args[]) throws Exception {

        try {
            final String className = PositionGeneratorTool.class.getSimpleName();
            String configFilename = Tool.getPersonalConfigFilename("config/"+className);
            PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);
            PositionGeneratorTool demo = new PositionGeneratorTool(prop);

            PositionGeneratorTool q = new PositionGeneratorTool(prop);
            q.log.info("Starting "+className);

            Thread.sleep(1000);

            demo.go();

            System.out.println("\n===");
            System.out.println("Initialization done.");
            System.out.println("Press enter to exit.");
            System.out.println("===\n");
            System.in.read();
            System.out.println("\nexiting...\n");

            Thread.sleep(1000);

            demo.close();

            q.log.info("Done with "+className);
            System.exit(0);

        } catch (RuntimeException r)
        {
            r.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    public final Logger log = LoggerFactory.getLogger(PositionGeneratorTool.class);

    @Override
    public void run() {
        Random rnd = new Random();
        while(!shutdownThread) {
            PositionSnapshot p = new PositionSnapshot();
            for(String account : accountList) {
                for(String pid : pidList) {
                    Decimal size = Decimal.fromDouble((int)(Math.random()*2000.0)-1000);
                    boolean leaveBlank = (size.isLessThan(Decimal.fromString("80")));
                    if(leaveBlank) continue;
                    if(size.isLessThan(Decimal.fromString("180"))) size=Decimal.ZERO;
                    AccountPosition ap = new AccountPosition(pid, account, size);
                    p.add(ap);
                }
            }
            PositionSnapshotMsg m = PositionSnapshotMsg.fromPositionSnapshot(p);
            senderPosition.publish(m.getTopic(), m);

//            System.out.println("Published: #"+counter+":"+lastData);
            log.info("Published "+counter+" data sets of "+p.size()+" positions.");
            counter++;
            Tool.sleepFor(interval);
        }
    }

    public void go() {
        thread.start();
    }

    public void close() {
        shutdownThread = true;
        senderPosition.close();
    }

    public PositionGeneratorTool(IProvideProperties prop)
    {
        interval = prop.getAsDecimal("interval").toInt();

        String pidListString=prop.get("productId");
        String[] pidParts = pidListString.split(",");
        pidList = Arrays.asList(pidParts);

        String accountsListString=prop.get("accounts");
        String[] accountParts = accountsListString.split(",");
        accountList = Arrays.asList(accountParts);

        productList = ProductList.createFromFile("config/CFDProductList.csv");
        Config config =  Config.fromProperties(prop);
        senderPosition = new Sender<PositionSnapshotMsg>(config.getExchangePositionSnapshot(), config.getServerIP());
        shutdownThread = false;
        thread = new Thread(this);
    }


    ///////////////////////////////////////////////////////////////////////////////////

    private Sender<PositionSnapshotMsg> senderPosition;
    private boolean shutdownThread;
    private Thread thread;
    private ProductList productList;
    private List<String> pidList;
    private List<String> accountList;
    private int counter;
    private int interval;

} // class

