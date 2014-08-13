package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class ExcelConnection implements IConsumeMarketData, IConsumeReceipt, DDEClientEventListener, IConsumeReports {

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
                    //TimeStamp
                    conversation.poke("R" + j + "C2",marketData.getTimestamp().toString());

                    //Lv0
                    conversation.poke("R" + j + "C3", marketData.getBidSize().toString());
                    conversation.poke("R" + j + "C4", marketData.getBid().toString());
                    conversation.poke("R" + j + "C5", marketData.getAskSize().toString());
                    conversation.poke("R" + j + "C6", marketData.getAsk().toString());

                    //Lv1
                    if(marketData.getBook().getDepth(BookSide.BID)==2) {
                        conversation.poke("R" + j + "C7", marketData.getBook().getBookRow(BookSide.BID, 1).getSize().toString());
                        conversation.poke("R" + j + "C8", marketData.getBook().getBookRow(BookSide.BID, 1).getPrice().toString());
                    }else{
                        conversation.poke("R" + j + "C7", "");
                        conversation.poke("R" + j + "C8", "");
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==2) {
                        conversation.poke("R" + j + "C9", marketData.getBook().getBookRow(BookSide.ASK, 1).getSize().toString());
                        conversation.poke("R" + j + "C10", marketData.getBook().getBookRow(BookSide.ASK, 1).getPrice().toString());
                    }else{
                        conversation.poke("R" + j + "C9", "");
                        conversation.poke("R" + j + "C10", "");
                    }

                    //Lv2
                    if(marketData.getBook().getDepth(BookSide.BID)==3) {
                        conversation.poke("R" + j + "C11", marketData.getBook().getBookRow(BookSide.BID, 2).getSize().toString());
                        conversation.poke("R" + j + "C12", marketData.getBook().getBookRow(BookSide.BID, 2).getPrice().toString());
                    }else{
                        conversation.poke("R" + j + "C11", "");
                        conversation.poke("R" + j + "C12", "");
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==3) {
                        conversation.poke("R" + j + "C13", marketData.getBook().getBookRow(BookSide.ASK, 2).getSize().toString());
                        conversation.poke("R" + j + "C14", marketData.getBook().getBookRow(BookSide.ASK, 2).getPrice().toString());
                    }else{
                        conversation.poke("R" + j + "C13", "");
                        conversation.poke("R" + j + "C14", "");
                    }

                    //Lv3
                    if(marketData.getBook().getDepth(BookSide.BID)==4) {
                        conversation.poke("R" + j + "C15", marketData.getBook().getBookRow(BookSide.BID, 3).getSize().toString());
                        conversation.poke("R" + j + "C16", marketData.getBook().getBookRow(BookSide.BID, 3).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==4) {
                        conversation.poke("R" + j + "C17", marketData.getBook().getBookRow(BookSide.ASK, 3).getSize().toString());
                        conversation.poke("R" + j + "C18", marketData.getBook().getBookRow(BookSide.ASK, 3).getPrice().toString());
                    }

                    //Lv4
                    if(marketData.getBook().getDepth(BookSide.BID)==5) {
                        conversation.poke("R" + j + "C19", marketData.getBook().getBookRow(BookSide.BID, 4).getSize().toString());
                        conversation.poke("R" + j + "C20", marketData.getBook().getBookRow(BookSide.BID, 4).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==5) {
                        conversation.poke("R" + j + "C21", marketData.getBook().getBookRow(BookSide.ASK, 4).getSize().toString());
                        conversation.poke("R" + j + "C22", marketData.getBook().getBookRow(BookSide.ASK, 4).getPrice().toString());
                    }

                    //Lv5
                    if(marketData.getBook().getDepth(BookSide.BID)==6) {
                        conversation.poke("R" + j + "C23", marketData.getBook().getBookRow(BookSide.BID, 5).getSize().toString());
                        conversation.poke("R" + j + "C24", marketData.getBook().getBookRow(BookSide.BID, 5).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==6) {
                        conversation.poke("R" + j + "C25", marketData.getBook().getBookRow(BookSide.ASK, 5).getSize().toString());
                        conversation.poke("R" + j + "C26", marketData.getBook().getBookRow(BookSide.ASK, 5).getPrice().toString());
                    }

                    //Lv6
                    if(marketData.getBook().getDepth(BookSide.BID)==7) {
                        conversation.poke("R" + j + "C27", marketData.getBook().getBookRow(BookSide.BID, 6).getSize().toString());
                        conversation.poke("R" + j + "C28", marketData.getBook().getBookRow(BookSide.BID, 6).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==7) {
                        conversation.poke("R" + j + "C29", marketData.getBook().getBookRow(BookSide.ASK, 6).getSize().toString());
                        conversation.poke("R" + j + "C30", marketData.getBook().getBookRow(BookSide.ASK, 6).getPrice().toString());
                    }

                    //Lv7
                    if(marketData.getBook().getDepth(BookSide.BID)==8) {
                        conversation.poke("R" + j + "C31", marketData.getBook().getBookRow(BookSide.BID, 7).getSize().toString());
                        conversation.poke("R" + j + "C32", marketData.getBook().getBookRow(BookSide.BID, 7).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==8) {
                        conversation.poke("R" + j + "C33", marketData.getBook().getBookRow(BookSide.ASK, 7).getSize().toString());
                        conversation.poke("R" + j + "C34", marketData.getBook().getBookRow(BookSide.ASK, 7).getPrice().toString());
                    }

                    //Lv8
                    if(marketData.getBook().getDepth(BookSide.BID)==9) {
                        conversation.poke("R" + j + "C35", marketData.getBook().getBookRow(BookSide.BID, 8).getSize().toString());
                        conversation.poke("R" + j + "C36", marketData.getBook().getBookRow(BookSide.BID, 8).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==9) {
                        conversation.poke("R" + j + "C37", marketData.getBook().getBookRow(BookSide.ASK, 8).getSize().toString());
                        conversation.poke("R" + j + "C38", marketData.getBook().getBookRow(BookSide.ASK, 8).getPrice().toString());
                    }

                    //Lv9
                    if(marketData.getBook().getDepth(BookSide.BID)==10) {
                        conversation.poke("R" + j + "C39", marketData.getBook().getBookRow(BookSide.BID, 9).getSize().toString());
                        conversation.poke("R" + j + "C40", marketData.getBook().getBookRow(BookSide.BID, 9).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==10) {
                        conversation.poke("R" + j + "C41", marketData.getBook().getBookRow(BookSide.ASK, 9).getSize().toString());
                        conversation.poke("R" + j + "C42", marketData.getBook().getBookRow(BookSide.ASK, 9).getPrice().toString());
                    }

                } catch (DDEException e) {
                    e.printStackTrace();
                }
            }
        }

        // use strategyToBusConnection.sendSettings(...) to send settings to the strategies like in onReport(..) below

    }

    @Override
    public void onReceipt(Receipt receipt) {

    }

    @Override
    public void onReport(IProvideProperties p) {

        // reports from strategies are coming in here. send them to Excel

        // for now writing to console:
        System.out.println("Strategy reports: "+PropertiesReader.toString(p));

        //lets send the report back as settings to test the way back to the strategy
        strategyToBusConnection.sendSettings(p);
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
            conversation.setTimeout(50000);
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

        if(Tool.isWindows()) startDDE();
        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        if(Tool.isWindows()) stopDDE();
        if(Tool.isWindows()) Thread.sleep(1000);
        if(!Tool.isWindows())Thread.sleep(60000);

        log.info("ExcelConnection done.");
        System.exit(0);
    }

    public ExcelConnection(IProvideProperties _prop)
    {
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setMarketDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        strategyToBusConnection.setReportsConsumer(this);
        if(Tool.isWindows()) {
            conversation = new DDEClientConversation();  // cant use this on Linux
            conversation.setEventListener(this);
        } else {
            System.out.println("This is not Windows! DDEClient will not work!");
//            System.exit(0);
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


    private Vector<String> currentProductIDs=new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;


} // class
