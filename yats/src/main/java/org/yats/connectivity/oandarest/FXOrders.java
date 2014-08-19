package org.yats.connectivity.oandarest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.MarketDataMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FXOrders {

    final Logger log = LoggerFactory.getLogger(FXRates.class);


    public String retrieve(String request) {
        HttpUriRequest httpGet = new HttpGet(serverUrl+request);
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+secret));

        HttpResponse resp = null;
        try {
            resp = httpClient.execute(httpGet);
            HttpEntity entity = resp.getEntity();

        if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
            InputStream stream = entity.getContent();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String all = "";
            while ((line = br.readLine()) != null) {
                all += line;
            }
            return all;
        }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void getOrders() {
        String res = retrieve("/v1/accounts/"+account+"/orders");
        log.info("orders:"+res);
    }

    public void getAccounts() {
        String res = retrieve("/v1/accounts?username="+username);
        log.info("accounts:"+res);
    }



    public void getRates() {
        try {

            HttpPost post = new HttpPost("https://stream-fxpractice.oanda.com/v1/accounts/3564938/orders");
            HttpUriRequest httpGet = new HttpGet("https://stream-fxpractice.oanda.com/v1/prices?accountId=3173292&instruments=USD_TRY,EUR_NZD,XAG_GBP,USD_PLN,USD_DKK,AUD_SGD,GBP_SGD,NZD_CHF,CHF_JPY,CHF_ZAR,XAU_SGD,UK10YB_GBP,CAD_JPY,EUR_USD,XAG_SGD,GBP_NZD,NZD_JPY,SGD_HKD,NZD_HKD,SUGAR_USD,XAU_EUR,XAU_GBP,SOYBN_USD,USB05Y_USD,EUR_HKD,USB30Y_USD,EUR_AUD,USD_JPY,USD_TWD,AUD_CHF,US30_USD,XAU_CAD,CORN_USD,WTICO_USD,US2000_USD,NZD_SGD,EUR_TRY,USD_INR,AUD_CAD,JP225_USD,EUR_NOK,XAG_CHF,XAU_JPY,XAU_NZD,GBP_HKD,AUD_NZD,FR40_EUR,XAG_CAD,USD_THB,XPD_USD,DE30_EUR,NZD_USD,CAD_CHF,USD_CZK,CAD_SGD,USD_SAR,NATGAS_USD,AUD_JPY,WHEAT_USD,AU200_AUD,USD_HKD,SPX500_USD,EU50_EUR,XAG_JPY,NZD_CAD,XAG_NZD,EUR_GBP,CH20_CHF,XPT_USD,SG30_SGD,XAG_USD,EUR_SGD,EUR_CZK,USD_SGD,USB10Y_USD,AUD_USD,ZAR_JPY,XAG_HKD,GBP_JPY,XAG_AUD,GBP_PLN,BCO_USD,SGD_JPY,XAU_USD,GBP_USD,NAS100_USD,USD_MXN,TRY_JPY,USD_CHF,NL25_EUR,XAU_XAG,XAG_EUR,EUR_ZAR,EUR_CHF,HKD_JPY,CAD_HKD,EUR_PLN,XAU_CHF,XCU_USD,USB02Y_USD,EUR_DKK,GBP_AUD,GBP_CHF,SGD_CHF,XAU_HKD,CHF_HKD,HK33_HKD,XAU_AUD,EUR_SEK,EUR_CAD,UK100_GBP,USD_HUF,USD_SEK,EUR_HUF,DE10YB_EUR,AUD_HKD,USD_CAD,GBP_ZAR,USD_CNY,USD_ZAR,EUR_JPY,USD_NOK,GBP_CAD");
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+secret));

            System.out.println("Executing request: " + httpGet.getRequestLine());

            HttpResponse resp = httpClient.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                InputStream stream = entity.getContent();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                while ((line = br.readLine()) != null) {

                    Object obj = JSONValue.parse(line);
                    JSONObject tick = (JSONObject) obj;

                    //ignore heartbeats
                    if (tick.containsKey("instrument")) {
                        System.out.println("-------");

                        String instrument = tick.get("instrument").toString();
                        String time = tick.get("time").toString();
                        double bid = Double.parseDouble(tick.get("bid").toString());
                        double ask = Double.parseDouble(tick.get("ask").toString());

                        System.out.println(instrument);
                        System.out.println(time);
                        System.out.println(bid);
                        System.out.println(ask);

                        MarketDataMsg data = new MarketDataMsg();

                        Sender<MarketDataMsg> sender = null;


                        Config config = Config.fromProperties(Config.createRealProperties());
                        sender = new Sender<MarketDataMsg>(config.getExchangeMarketData(), config.getServerIP());

                        data.productId=instrument;
                        data.bid= Decimal.fromDouble(bid).toString();
                        data.ask= Decimal.fromDouble(ask).toString();
                        data.bidSize="1";
                        data.askSize="1";
                        data.timestamp= Tool.getUTCTimestampString();
                        sender.publish(data.getTopic(), data);

                    }
                }
            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public FXOrders() {
        httpClient = new DefaultHttpClient();
        account="3564938";
        secret="37e6eba904c5a0599c364647b5bb39ed-49b4e985887799ca2a4fb4724b5ebda4";
//        serverUrl="https://stream-fxpractice.oanda.com";
        serverUrl="https://api-fxpractice.oanda.com";
        username="annalange";
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private String account;
    private DefaultHttpClient httpClient;
    private String secret;
    private String serverUrl;
    private String username;

    public static void main (String[]args) throws IOException {

        FXOrders fx = new FXOrders();
        fx.getAccounts();
        fx.getOrders();

    }

} // class
