package org.yats.connectivity.oandarest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.IProvidePriceFeed;
import org.yats.trading.IProvideProduct;
import org.yats.trading.MarketData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class PriceFeed implements IProvidePriceFeed, Runnable {

    final Logger log = LoggerFactory.getLogger(PriceFeed.class);


    @Override
    public void subscribe(String productId, IConsumeMarketData consumer) {
        marketDataConsumer = consumer;
        if(subscriptionList.containsKey(productId)) return;
        if(!properties.exists(productId)) {
            log.debug("Subscription not available:"+productId);
            return;
        }
        log.debug("New subscription:"+productId);
        subscriptionList.put(productId,consumer);
        String symbol = properties.get(productId);
        mapPidToSymbol.put(productId, symbol);
        mapSymbolToPid.put(symbol, productId);
        stopReceiving=true;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        while(!shutdown)
        {
            try {
                receive();
                stopReceiving=false;
                httpClient.getConnectionManager().shutdown();
                httpClient = new DefaultHttpClient();
                Tool.sleepFor(500);
            }
            catch(IOException e) {
                log.error("Network error accessing Oanda.");
                Tool.sleepFor(1000);
            }
        }
        running=false;
    }

    public void logon() {
        accountId = properties.get("externalAccount");
        secret = properties.get("secret");
        thread.start();
    }

    private static final String UrlGetPricesPrefix = "https://stream-fxpractice.oanda.com/v1/prices?";

    private String getInstrumentString() {
        StringBuilder b = new StringBuilder();
        for(String symbol : mapPidToSymbol.values()) {
            b.append(symbol);
            b.append(",");
        }
        return b.toString();
    }

    private void receive() throws IOException {
        String instruments = getInstrumentString();
        if(instruments.length()==0) return;

        HttpUriRequest httpGet = new HttpGet(UrlGetPricesPrefix+"accountId="+accountId+"&instruments="+instruments);
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+secret));

        System.out.println("Executing request: " + httpGet.getRequestLine());


        HttpResponse resp = httpClient.execute(httpGet);
        HttpEntity entity = resp.getEntity();

        if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
            InputStream stream = entity.getContent();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            while ((line = br.readLine()) != null) {
                if(stopReceiving) return;
                Object obj = JSONValue.parse(line);
                JSONObject tick = (JSONObject) obj;

                if(line.contains("instrument")) {
                //ignore heartbeats
//                if (tick.containsKey("instrument")) {
                    System.out.println("-------");

                    JSONObject values = (JSONObject)tick.get("tick");
                    String instrument = values.get("instrument").toString();
                    String time = values.get("time").toString();
                    double bid = Double.parseDouble(values.get("bid").toString());
                    double ask = Double.parseDouble(values.get("ask").toString());
                    Decimal last = Decimal.fromDouble((bid+ask)/2);

                    System.out.println(instrument);
                    System.out.println(time);
                    System.out.println(bid);
                    System.out.println(ask);

                    String productId = mapSymbolToPid.get(instrument);
                    MarketData data = new MarketData(Tool.getUTCTimestamp(),
                            productId, Decimal.fromDouble(bid), Decimal.fromDouble(ask), last,
                            Decimal.ONE, Decimal.ONE, Decimal.ONE);

                    marketDataConsumer.onMarketData(data);

//                        Sender<MarketDataMsg> sender = null;
//                        sender = new Sender<MarketDataMsg>(Config.DEFAULT.getExchangeMarketData(),
//                                Config.DEFAULT.getServerIP());
//                        data.productId=instrument;
//                        data.bid= Decimal.fromDouble(bid).toString();
//                        data.ask= Decimal.fromDouble(ask).toString();
//                        data.bidSize="1";
//                        data.askSize="1";
//                        data.timestamp= Tool.getUTCTimestampString();
//                        sender.publish(data.getTopic(), data);

                }
            }
        } else {
            // print error message
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseString);
        }

    }

    public static PriceFeed createFromPropertiesReader(PropertiesReader prop) {
        return new PriceFeed(prop);
    }

    public void shutdown() {
        shutdown = true;
        stopReceiving = true;
    }

    public PriceFeed(IProvideProperties properties) {
        this.properties = properties;
        mapPidToSymbol = new ConcurrentHashMap<String, String>();
        subscriptionList = new ConcurrentHashMap<String, IConsumeMarketData>();
        mapSymbolToPid = new ConcurrentHashMap<String, String>();
        thread = new Thread(this);
        httpClient = new DefaultHttpClient();
        stopReceiving = false;
        shutdown = false;
        running=true;
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    private boolean running;
    private boolean stopReceiving;
    private boolean shutdown;
    private DefaultHttpClient httpClient;
    private Thread thread;
    private String accountId;
    private String secret;
    private IProvideProperties properties;
    private IProvideProduct productProvider;
    private ConcurrentHashMap<String, IConsumeMarketData> subscriptionList;
    private ConcurrentHashMap<String, String> mapPidToSymbol;
    private ConcurrentHashMap<String, String> mapSymbolToPid;
    IConsumeMarketData marketDataConsumer;

}
