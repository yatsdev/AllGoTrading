package org.yats.trader.examples.strategies;


import com.espertech.esper.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.trader.StrategyBase;
import org.yats.trading.PriceData;
import org.yats.trading.PriceDataInDoubleFormat;
import org.yats.trading.Receipt;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PriceCheck extends StrategyBase{

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceCheck.class);


    String lastBigChange="";

    @Override
    public void onPriceDataForStrategy(PriceData priceData)
    {
        if(shuttingDown) return;
        if(!isInitialised()) return;
        if(!lastPrices.containsKey(priceData.getProductId())) return;

        String key = priceData.getProductId();

        boolean hugeLastChangeUp = priceData.getLast().isGreaterThan(lastPrices.get(key).getLast().multiply(upMove));
        boolean hugeBidChangeUp = priceData.getBid().isGreaterThan(lastPrices.get(key).getBid().multiply(upMove));
        boolean hugeAskChangeUp = priceData.getAsk().isGreaterThan(lastPrices.get(key).getAsk().multiply(upMove));
        boolean hugeLastChangeDown = priceData.getLast().isLessThan(lastPrices.get(key).getLast().multiply(downMove));
        boolean hugeBidChangeDown = priceData.getBid().isLessThan(lastPrices.get(key).getBid().multiply(downMove));
        boolean hugeAskChangeDown = priceData.getAsk().isLessThan(lastPrices.get(key).getAsk().multiply(downMove));
        boolean hugeChange =  (hugeLastChangeUp || hugeBidChangeUp || hugeAskChangeUp
                || hugeLastChangeDown || hugeBidChangeDown || hugeAskChangeDown);

        if(hugeChange) {
            System.out.println("");
            log.info("Huge change in price! " + priceData.toString() + " last:" + lastPrices.get(key).toString());
            lastBigChange = priceData.toString();

            setReport("lastBigChange", lastBigChange);
            sendReports();



        } else {
            dots++;
            if(dots>80) {
                System.out.println("");
                dots=0;
            }
            System.out.print(".");
        }
        lastPrices.put(key, priceData);



        PriceDataInDoubleFormat priceDataInDoubleFormat=new PriceDataInDoubleFormat(priceData);

        cepRT.sendEvent(priceDataInDoubleFormat);

    }

    @Override
    public void onReceiptForStrategy(Receipt receipt)
    {
    }

    @Override
    public void onStopStrategy() {
    }

    @Override
    public void onStartStrategy() {
    }

    @Override
    public void onSettingsForStrategy(IProvideProperties p) {
        addReports(p);
        setReport("lastBigChange", lastBigChange);
        sendReports();
    }



    @Override
    public void onInitStrategy()
    {
        setInternalAccount(getConfig("internalAccount"));
        tradeProductIds = getConfig("tradeProductIds");
        String[] parts = tradeProductIds.split(",");
        tradeProductIdsNameList = Arrays.asList(parts);

        for(int i=0;i<tradeProductIdsNameList.size();i++){
            subscribe(tradeProductIdsNameList.get(i));
            lastPrices.put(tradeProductIdsNameList.get(i),PriceData.createFromLast(tradeProductIdsNameList.get(i),Decimal.ZERO));
        }

        upMove = Decimal.fromString(getConfig("upMove","1.000001"));
        downMove = Decimal.fromString(getConfig("downMove","0.999999"));


    }

    @Override
    public void onShutdown()
    {
        shuttingDown=true;
    }

    public PriceCheck() {
        super();
        lastPrice = PriceData.NULL;
        shuttingDown=false;

        //The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("PriceDataInDoubleFormat", PriceDataInDoubleFormat.class.getName());
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
        cepRT = cep.getEPRuntime();

        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select avg(ask) from PriceDataInDoubleFormat.win:time(3 min)"); //
        cepStatement.addListener(new CEPListener());


    }



    private PriceData lastPrice;
    private EPRuntime cepRT;

    private ConcurrentHashMap<String,PriceData> lastPrices=new ConcurrentHashMap();
    private boolean shuttingDown;
    private String tradeProductIds;
    private static int dots = 0;
    private List<String> tradeProductIdsNameList;

    private Decimal upMove;
    private Decimal downMove;


    private class CEPListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
            System.out.println("Event received: " + newData[0].getUnderlying());
        }
    }

} // class

