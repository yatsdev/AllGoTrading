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


                    for(int n=1;n<10;n++){

                        int p=n+1;

                        String lvnBidSize=new String("");
                        String lvnBidPrice=new String("");
                        String lvnAskSize=new String("");
                        String lvnAskPrice=new String("");

                        if(marketData.getBook().getDepth(BookSide.BID)>=p) {
                            lvnBidSize=marketData.getBook().getBookRow(BookSide.BID, n).getSize().toString();
                            lvnBidPrice=marketData.getBook().getBookRow(BookSide.BID, n).getPrice().toString();
                        }

                        if(marketData.getBook().getDepth(BookSide.ASK)>=p) {
                            lvnAskSize = marketData.getBook().getBookRow(BookSide.ASK, n).getSize().toString();
                            lvnAskPrice = marketData.getBook().getBookRow(BookSide.ASK, n).getPrice().toString();
                        }

                        marketDataString=marketDataString+"\t"+lvnBidSize+"\t"+lvnBidPrice+"\t"+lvnAskSize+"\t"+lvnAskPrice;



                    }

//                    log.info(marketDataString);
                    if(shutdown) return;
                    conversation.poke("R"+j+"C2:R"+j+"C42",marketDataString);
                   

                } catch (DDEException e) {
                    e.printStackTrace();
                    System.exit(-1);
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



        try {

            String StrategyNamesString=conversationReports.request("C1");
            parseStrategyNames(StrategyNamesString);
            String KeyValuesString=conversationReports.request("R1");
            parsekeyValues(KeyValuesString);

           Vector<keyvalue> vectorkeyvalue=new Vector<keyvalue>();

            for(String key : p.getKeySet()) {
                keyvalue kv=new keyvalue();
                String value = p.get(key);
                kv.setKey(key);
                kv.setValue(value);
                vectorkeyvalue.add(kv);


////                //adding key/values not present on R1
////                  if(!(KeyValues.contains(key))){
////                        conversationReports.poke("R1C"+KeyValues.size(),key);
//                    }

            }


         for ( int q=0;q<StrategyNames.size();q++){
             int position=q+1;
             if(StrategyNames.elementAt(q).compareTo(p.get("strategyName"))==0){
                 conversationReports.poke("R"+position+"C2:R"+position+"C55",generatePerRowPokeString(KeyValues,vectorkeyvalue));
             }
         }





        } catch (DDEException e) {
            e.printStackTrace();
        }




       // reports from strategies are coming in here. send them to Excel

        // for now writing to console:
        System.out.println("Strategy reports: "+PropertiesReader.toString(p));

        //lets send the report back as settings to test the way back to the strategy
//        strategyToBusConnection.sendSettings(p);
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
            conversation.connect("Excel", prop.get("DDEPathToExcelFile"));

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
            shutdown = true;
            Tool.sleepFor(500);
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
        shutdown=false;
        prop = _prop;
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

    private void parseStrategyNames(String strategyNames) {
        String[] parts = strategyNames.split("\r\n");
        StrategyNames = new Vector<String>(Arrays.asList(parts));

    }

    private void parsekeyValues(String keyValues) {
        String[] parts = keyValues.split("\t");
        KeyValues = new Vector<String>(Arrays.asList(parts));

    }



    private String generatePerRowPokeString(Vector<String> KeyValues,Vector<keyvalue> vectorkeyvalue){

        String PerRowPokeString = "";

     for(int i=0;i<KeyValues.size();i++){//R1C1 is empty
          for(int j=0;j<vectorkeyvalue.size();j++){
              if(KeyValues.elementAt(i).compareTo(vectorkeyvalue.elementAt(j).getKey())==0){
                  if(j==0){
                      PerRowPokeString=vectorkeyvalue.elementAt(j).getValue();
                  }
                  PerRowPokeString=PerRowPokeString+"\t"+vectorkeyvalue.elementAt(j).getValue();

              }else{

                  //PerRowPokeString=PerRowPokeString+"\t"+"";
              }


          }
     }


return PerRowPokeString;
    }


    private Vector<String> currentProductIDs=new Vector<String>();
    private Vector<String> StrategyNames=new Vector<String>();
    private Vector<String> KeyValues =new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;
    private DDEClientConversation conversationReports;
    private IProvideProperties prop;
    private boolean shutdown;


} // class

class keyvalue{
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String key;
    private String value;
        }