package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.MarketData;
import org.yats.trading.Receipt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class ExcelConnection implements IConsumeMarketData, IConsumeReceipt, DDEClientEventListener {

    final Logger log = LoggerFactory.getLogger(ExcelConnection.class);

    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect()");
    }

    @Override
    public void onItemChanged(String s, String s2, String s3)
    {
        parseProductIds(s3);
        subscribeAllProductIds();
    }

    @Override
    public void onMarketData(MarketData marketData) {
        for (int i = 1; i < currentProductIDs.size(); i++) {
            int j = i + 1;
            if (marketData.hasProductId(currentProductIDs.elementAt(i))){
                try {
                    conversation.poke("R" + j + "C2", marketData.getBid().toString());
                    conversation.poke("R" + j + "C3", marketData.getAsk().toString());
                } catch (DDEException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onReceipt(Receipt receipt) {

    }

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    public void subscribe(String pid)
    {
        strategyToBusConnection.subscribe(pid, this);
    }

    public boolean isExcelRunning() {
        return false;
    }

    public boolean isWorkbookOpenInExcel() {
        return false;
    }

    public void startExcelWithWorkbook() {
    }

    public void startDDE() {
        try
        {
            System.out.print("conversation.connect...");
            conversation.connect("Excel", "MarketData");
            System.out.println("done.");
            System.out.print("conversation.request...");
            String s = conversation.request("C1");
            System.out.println("done.");
            parseProductIds(s);
            subscribeAllProductIds();
            System.out.print("conversation.startAdvice...");
            conversation.startAdvice("C1");
            System.out.println("done.");
        }
        catch (DDEMLException e)
        {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode())
                    + " " + e.getMessage());
        }
        catch (DDEException e)
        {
            System.out.println("DDEException: " + e.getMessage());
        }
    }

    public void stopDDE()
    {
        try {
            conversation.stopAdvice("C1");//This means all elements in column 1
            conversation.disconnect();
        } catch (DDEException e) {
            e.printStackTrace();
        }
    }

    public void go() throws InterruptedException, IOException {
        log.info("Starting ExcelConnection...");

//        thread = new Thread(this);
//        thread.start();

        startDDE();
        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        stopDDE();
        Thread.sleep(1000);

        log.info("ExcelConnection done.");
        System.exit(0);
    }

    public ExcelConnection(IProvideProperties _prop)
    {
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setMarketDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        if(Tool.isWindows()) {
            conversation = new DDEClientConversation();  // cant use this on Linux
            conversation.setEventListener(this);
        } else {
            System.out.println("This is not Windows! DDEClient will not work!");
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private void subscribeAllProductIds()
    {
        for(String pid : currentProductIDs)
        {
            subscribe(pid);
        }
    }

    private void parseProductIds(String s3) {
        String[] parts = s3.split("\r\n");
        currentProductIDs = new Vector<String>(Arrays.asList(parts));
    }


    private Vector<String> productIDs=new Vector<String>();
    private Vector<String> currentProductIDs=new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;
    private int j;


} // class
