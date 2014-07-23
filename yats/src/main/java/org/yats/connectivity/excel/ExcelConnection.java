package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class ExcelConnection implements Runnable {

    private Vector<String> productIDs=new Vector<String>();

    private Vector<String> currentProductIDs=new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection= new StrategyToBusConnection(Config.createRealProperties()); //.createRealProperties() does not work



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

    @Override
    public void run() {


        try
        {
            final DDEClientConversation conversation = new DDEClientConversation();

            conversation.setEventListener(new DDEClientEventListener()
            {
                public void onDisconnect()
                {
                    System.out.println("onDisconnect()");
                }

                public void onItemChanged(String topic, String item, String data) {
                    String[] parts = data.split("\r\n");
                    currentProductIDs = new Vector<String>(Arrays.asList(parts));

                    if (productIDs.isEmpty()) {
                        for (int i = 1; i < currentProductIDs.size(); i++) {
                            int j=i+1;
                            try {
                                conversation.poke("R" + j + "C2", "miao");//Substitute with Bid Price Data Stream
                                conversation.poke("R" + j + "C3", "ciao");//Substitute with Ask Price Data Stream
                            } catch (DDEException e) {
                                e.printStackTrace();
                            }

                        }

                    }

                    /*else {
                        for (int i = 1; i < currentProductIDs.size() ; i++) {
                            if (!(currentProductIDs.elementAt(i).compareTo(productIDs.elementAt(i)) == 0)) {
                                int j=i+1;
                                try {
                                    conversation.poke("R" + j + "C2", "");
                                    conversation.poke("R" + j + "C3", "");
                                } catch (DDEException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    }*/


                }

            });

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

    public ExcelConnection(String excelFileName) {
        this.excelFileName = excelFileName;
        thread = new Thread(this);
    }

    private String excelFileName;
    private Thread thread;
} // class
