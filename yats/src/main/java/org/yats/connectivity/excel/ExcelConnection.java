package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.Config;
import org.yats.trading.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class ExcelConnection implements Runnable, IConsumeMarketData, IConsumeReceipt, DDEClientEventListener {



    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect()");
    }

    @Override
    public void onItemChanged(String s, String s2, String s3)
    {
        String[] parts = s.split("\r\n");
        currentProductIDs = new Vector<String>(Arrays.asList(parts));

        if (productIDs.isEmpty()) {
            for (int i = 1; i < currentProductIDs.size(); i++) {
                int j=i+1;
                try {
                    strategyToBusConnection.subscribe("4663789", this);
                    //connection.subscribe(productId, consumer);
                    conversation.poke("R" + j + "C2", "miao");//Substitute with Bid Price Data Stream
                    conversation.poke("R" + j + "C3", "ciao");//Substitute with Ask Price Data Stream
                } catch (DDEException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onMarketData(MarketData marketData)
    {
        System.out.println("Here! Something happens! ;)");
        System.out.println("But you have to start the FIXclient or OandaClient to receive data first!");
        System.out.println(marketData.getAsk() + "");
    }

    @Override
    public void onReceipt(Receipt receipt) {

    }

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    @Override
    public void run()
    {
        try
        {
            conversation.connect("Excel", "MarketData");
            try
            {
                conversation.startAdvice("C1");

                System.out.println("Press Enter to quit");
                System.in.read();

                conversation.stopAdvice("C1");//This means all elements in column 1
            } catch (IOException e) {
                e.printStackTrace();
            } finally
            {
                conversation.disconnect();
            }

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

    public void startExcelLink() {
        thread.start();
    }


    public ExcelConnection(String excelFileName)
    {
        this.excelFileName = excelFileName;
        strategyToBusConnection = new StrategyToBusConnection(Config.createRealProperties());
        strategyToBusConnection.setMarketDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        if(Tool.isWindows()) {
            conversation = new DDEClientConversation();  // cant use this on Linux
            conversation.setEventListener(this);
        } else {
            System.out.println("This is not Windows! DDEClient will not work!");
            //System.exit(0); // for now commented out. But should be active later.
            // Would be neat though if the server can run on Linux and communicate with Excel on a remote Windows machine
        }


        thread = new Thread(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private String excelFileName;
    private Thread thread;
    private Vector<String> productIDs=new Vector<String>();
    private Vector<String> currentProductIDs=new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;



} // class
