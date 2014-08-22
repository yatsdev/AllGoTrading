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

                    String marketDataString=new String(marketData.getTimestamp().toString()+"\t"
                            +marketData.getBidSize().toString()+"\t"
                            +marketData.getBid().toString()+"\t"
                            +marketData.getAskSize().toString()
                            +"\t"+marketData.getAsk().toString());


                   //Lv1
                    String lv1BidSize=new String("");
                    String lv1BidPrice=new String("");
                    String lv1AskSize=new String("");
                    String lv1AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=2) {
                         lv1BidSize=marketData.getBook().getBookRow(BookSide.BID, 1).getSize().toString();
                         lv1BidPrice=marketData.getBook().getBookRow(BookSide.BID, 1).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=2) {
                        lv1AskSize = marketData.getBook().getBookRow(BookSide.ASK, 1).getSize().toString();
                        lv1AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 1).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv1BidSize+"\t"+lv1BidPrice+"\t"+lv1AskSize+"\t"+lv1AskPrice;

                   //Lv2
                    String lv2BidSize=new String("");
                    String lv2BidPrice=new String("");
                    String lv2AskSize=new String("");
                    String lv2AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=3) {
                        lv2BidSize=marketData.getBook().getBookRow(BookSide.BID, 2).getSize().toString();
                        lv2BidPrice=marketData.getBook().getBookRow(BookSide.BID, 2).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=3) {
                        lv2AskSize = marketData.getBook().getBookRow(BookSide.ASK, 2).getSize().toString();
                        lv2AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 2).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv2BidSize+"\t"+lv2BidPrice+"\t"+lv2AskSize+"\t"+lv2AskPrice;

                    //Lv3
                    String lv3BidSize=new String("");
                    String lv3BidPrice=new String("");
                    String lv3AskSize=new String("");
                    String lv3AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=4) {
                        lv3BidSize=marketData.getBook().getBookRow(BookSide.BID, 3).getSize().toString();
                        lv3BidPrice=marketData.getBook().getBookRow(BookSide.BID, 3).getPrice().toString();

                        if(lv3BidSize.length()==0) log.info("lvl3bidSize empty");
                        if(lv3BidPrice.length()==0) log.info("lvl3bidPrice empty");
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=4) {
                        lv3AskSize = marketData.getBook().getBookRow(BookSide.ASK, 3).getSize().toString();
                        lv3AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 3).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv3BidSize+"\t"+lv3BidPrice+"\t"+lv3AskSize+"\t"+lv3AskPrice;


                    //Lv4
                    String lv4BidSize=new String("");
                    String lv4BidPrice=new String("");
                    String lv4AskSize=new String("");
                    String lv4AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=5) {
                        lv4BidSize=marketData.getBook().getBookRow(BookSide.BID, 4).getSize().toString();
                        lv4BidPrice=marketData.getBook().getBookRow(BookSide.BID, 4).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=5) {
                        lv4AskSize = marketData.getBook().getBookRow(BookSide.ASK, 4).getSize().toString();
                        lv4AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 4).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv4BidSize+"\t"+lv4BidPrice+"\t"+lv4AskSize+"\t"+lv4AskPrice;

                    //Lv5
                    String lv5BidSize=new String("");
                    String lv5BidPrice=new String("");
                    String lv5AskSize=new String("");
                    String lv5AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=6) {
                        lv5BidSize=marketData.getBook().getBookRow(BookSide.BID, 5).getSize().toString();
                        lv5BidPrice=marketData.getBook().getBookRow(BookSide.BID, 5).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=6) {
                        lv5AskSize = marketData.getBook().getBookRow(BookSide.ASK, 5).getSize().toString();
                        lv5AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 5).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv5BidSize+"\t"+lv5BidPrice+"\t"+lv5AskSize+"\t"+lv5AskPrice;

                    //Lv6
                    String lv6BidSize=new String("");
                    String lv6BidPrice=new String("");
                    String lv6AskSize=new String("");
                    String lv6AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=7) {
                        lv6BidSize=marketData.getBook().getBookRow(BookSide.BID, 6).getSize().toString();
                        lv6BidPrice=marketData.getBook().getBookRow(BookSide.BID, 6).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=7) {
                        lv6AskSize = marketData.getBook().getBookRow(BookSide.ASK, 6).getSize().toString();
                        lv6AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 6).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv6BidSize+"\t"+lv6BidPrice+"\t"+lv6AskSize+"\t"+lv6AskPrice;

                    //Lv7
                    String lv7BidSize=new String("");
                    String lv7BidPrice=new String("");
                    String lv7AskSize=new String("");
                    String lv7AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=8) {
                        lv7BidSize=marketData.getBook().getBookRow(BookSide.BID, 7).getSize().toString();
                        lv7BidPrice=marketData.getBook().getBookRow(BookSide.BID, 7).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=8) {
                        lv7AskSize = marketData.getBook().getBookRow(BookSide.ASK, 7).getSize().toString();
                        lv7AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 7).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv7BidSize+"\t"+lv7BidPrice+"\t"+lv7AskSize+"\t"+lv7AskPrice;

                    //Lv8
                    String lv8BidSize=new String("");
                    String lv8BidPrice=new String("");
                    String lv8AskSize=new String("");
                    String lv8AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=9) {
                        lv8BidSize=marketData.getBook().getBookRow(BookSide.BID, 8).getSize().toString();
                        lv8BidPrice=marketData.getBook().getBookRow(BookSide.BID, 8).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=9) {
                        lv8AskSize = marketData.getBook().getBookRow(BookSide.ASK, 8).getSize().toString();
                        lv8AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 8).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv8BidSize+"\t"+lv8BidPrice+"\t"+lv8AskSize+"\t"+lv8AskPrice;

                    //Lv9
                    String lv9BidSize=new String("");
                    String lv9BidPrice=new String("");
                    String lv9AskSize=new String("");
                    String lv9AskPrice=new String("");

                    if(marketData.getBook().getDepth(BookSide.BID)>=10) {
                        lv9BidSize=marketData.getBook().getBookRow(BookSide.BID, 9).getSize().toString();
                        lv9BidPrice=marketData.getBook().getBookRow(BookSide.BID, 9).getPrice().toString();
                    }

                    if(marketData.getBook().getDepth(BookSide.ASK)>=10) {
                        lv9AskSize = marketData.getBook().getBookRow(BookSide.ASK, 9).getSize().toString();
                        lv9AskPrice = marketData.getBook().getBookRow(BookSide.ASK, 9).getPrice().toString();
                    }

                    marketDataString=marketDataString+"\t"+lv9BidSize+"\t"+lv9BidPrice+"\t"+lv9AskSize+"\t"+lv9AskPrice;

                    log.info(marketDataString);
                    conversation.poke("R"+j+"C2:R"+j+"C42",marketDataString);
                   

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
            close();
            System.exit(-1);
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

    public void close() {
        strategyToBusConnection.close();
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

        close();
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
            try {
                conversation = new DDEClientConversation();  // cant use this on Linux
                conversation.setEventListener(this);
            } catch(UnsatisfiedLinkError  e){
                log.error(e.getMessage());
                close();
                System.exit(-1);
            }
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
