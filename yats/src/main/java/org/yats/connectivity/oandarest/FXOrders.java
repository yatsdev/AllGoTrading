package org.yats.connectivity.oandarest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.MarketDataMsg;
import org.yats.trading.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FXOrders implements ISendOrder, Runnable {

    final Logger log = LoggerFactory.getLogger(FXOrders.class);


    public void getOrders() {
        String res = retrieve("/v1/accounts/"+ getOandaAccount()+"/orders");
        log.info("orders:"+res);
    }

    public void getAccounts() {
        String res = retrieve("/v1/accounts?username="+getUserName());
        log.info("accounts:"+res);
    }

    @Override
    public void sendOrderNew(OrderNew orderNew) {
        String oandaSymbol = prop.get(orderNew.getProductId());
        HttpPost post = new HttpPost(getServerUrlApi()+"/v1/accounts/"+ getOandaAccount()+"/orders");
        post.setHeader(new BasicHeader("Authorization", "Bearer "+getSecret()));
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("instrument",oandaSymbol));
        long size = (long) orderNew.getSize().roundToDigits(0).toDouble();
        nameValuePairs.add(new BasicNameValuePair("units",""+size));
        nameValuePairs.add(new BasicNameValuePair("side", orderNew.getBookSide().toBuySellString()));
        nameValuePairs.add(new BasicNameValuePair("type","limit"));
        nameValuePairs.add(new BasicNameValuePair("price",orderNew.getLimit().toString()));
        DateTime expiry = Tool.getUTCTimestamp().plusMonths(1);
        String expiryString = expiry.toString();
        // format:"2014-09-01T00:00:00Z"
        nameValuePairs.add(new BasicNameValuePair("expiry",expiryString));
        //instrument=EUR_USD&units=2&side=sell&type=marketIfTouched&price=1.2&expiry=2013-04-01T00%3A00%3A00Z
        HttpResponse response=null;
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpPoll.execute(post);
            boolean orderAccepted = (response.getStatusLine().toString().contains("CREATED"));
            if(!orderAccepted) {
                    EntityUtils.consume(response.getEntity());
                    return;
            }
            String oandaOrderId = extractServerOrderId(response);
            log.debug("created Oanda order with orderId="+orderNew.getOrderId()+" oandaId=" + oandaOrderId);
            oandaId2OrderMap.put(oandaOrderId, orderNew);
            orderId2OandaIdMap.put(orderNew.getOrderId().toString(), oandaOrderId);
            EntityUtils.consume(response.getEntity());
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response!=null && response.getEntity()!=null) {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @Override
    public void sendOrderCancel(OrderCancel orderCancel) {
        boolean orderIdKnown = orderId2OandaIdMap.containsKey(orderCancel.getOrderId().toString());
        if(!orderIdKnown) {
            String rejection = "No Oanda order id found for "+orderCancel.getOrderId();
            Receipt r = orderCancel.createReceiptDefault()
                    .withRejectReason(rejection)
                    .withEndState(true)
                    ;
            log.error(rejection);
            receiptConsumer.onReceipt(r);
        }
        String oandaOrderId = orderId2OandaIdMap.get(orderCancel.getOrderId().toString());


        String urlString = getServerUrlApi()+"/v1/accounts/"+ getOandaAccount()+"/orders/"+oandaOrderId;
        HttpUriRequest httpGet = new HttpDelete(urlString);
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+getSecret()));

        HttpResponse resp = null;
        try {
            resp = httpPoll.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                log.info("Canceled order with orderId="+orderCancel.getOrderId()+" oandaId="+oandaOrderId);
                OrderNew orderNew = oandaId2OrderMap.get(oandaOrderId);
                orderId2OandaIdMap.remove(orderCancel.getOrderId().toString());
                oandaId2OrderMap.remove(oandaOrderId);
                Receipt r = orderNew.createReceiptDefault().withEndState(true);
                receiptConsumer.onReceipt(r);
            }
            if(entity!=null) EntityUtils.consume(entity);
            return;
        } catch (IOException e) {
            e.printStackTrace();

            if(resp!=null && resp.getEntity()!=null) {
                try {
                    EntityUtils.consume(resp.getEntity());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            throw new CommonExceptions.NetworkException("Error canceling order "
                    +orderCancel.getOrderId().toString()+" : "+e.getMessage());
        }
    }

    private String extractServerOrderId(HttpResponse response) {
        String serverOrderId="";
        for(Header h : response.getHeaders("Location")) {
            String[] parts =  h.getValue().split("/");
            if(parts.length<1) throw new CommonExceptions.FieldNotFoundException("Short response! No serverOrderId found in response from Oanda! "+response.toString());
            serverOrderId = parts[parts.length-1];
        }
        if(serverOrderId.length()==0)
            throw new CommonExceptions.FieldNotFoundException("No serverOrderId found in response from Oanda! "+response.toString());
        return serverOrderId;
    }

    public void getRates() {
        try {

            HttpPost post = new HttpPost("https://stream-fxpractice.oanda.com/v1/accounts/3564938/orders");
            HttpUriRequest httpGet = new HttpGet("https://stream-fxpractice.oanda.com/v1/prices?accountId=3173292&instruments=USD_TRY,EUR_NZD,XAG_GBP,USD_PLN,USD_DKK,AUD_SGD,GBP_SGD,NZD_CHF,CHF_JPY,CHF_ZAR,XAU_SGD,UK10YB_GBP,CAD_JPY,EUR_USD,XAG_SGD,GBP_NZD,NZD_JPY,SGD_HKD,NZD_HKD,SUGAR_USD,XAU_EUR,XAU_GBP,SOYBN_USD,USB05Y_USD,EUR_HKD,USB30Y_USD,EUR_AUD,USD_JPY,USD_TWD,AUD_CHF,US30_USD,XAU_CAD,CORN_USD,WTICO_USD,US2000_USD,NZD_SGD,EUR_TRY,USD_INR,AUD_CAD,JP225_USD,EUR_NOK,XAG_CHF,XAU_JPY,XAU_NZD,GBP_HKD,AUD_NZD,FR40_EUR,XAG_CAD,USD_THB,XPD_USD,DE30_EUR,NZD_USD,CAD_CHF,USD_CZK,CAD_SGD,USD_SAR,NATGAS_USD,AUD_JPY,WHEAT_USD,AU200_AUD,USD_HKD,SPX500_USD,EU50_EUR,XAG_JPY,NZD_CAD,XAG_NZD,EUR_GBP,CH20_CHF,XPT_USD,SG30_SGD,XAG_USD,EUR_SGD,EUR_CZK,USD_SGD,USB10Y_USD,AUD_USD,ZAR_JPY,XAG_HKD,GBP_JPY,XAG_AUD,GBP_PLN,BCO_USD,SGD_JPY,XAU_USD,GBP_USD,NAS100_USD,USD_MXN,TRY_JPY,USD_CHF,NL25_EUR,XAU_XAG,XAG_EUR,EUR_ZAR,EUR_CHF,HKD_JPY,CAD_HKD,EUR_PLN,XAU_CHF,XCU_USD,USB02Y_USD,EUR_DKK,GBP_AUD,GBP_CHF,SGD_CHF,XAU_HKD,CHF_HKD,HK33_HKD,XAU_AUD,EUR_SEK,EUR_CAD,UK100_GBP,USD_HUF,USD_SEK,EUR_HUF,DE10YB_EUR,AUD_HKD,USD_CAD,GBP_ZAR,USD_CNY,USD_ZAR,EUR_JPY,USD_NOK,GBP_CAD");
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+secret));

            System.out.println("Executing request: " + httpGet.getRequestLine());

            HttpResponse resp = httpPoll.execute(httpGet);
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
            httpPoll.getConnectionManager().shutdown();
        }
    }

    @Override
    public void run() {
        HttpUriRequest httpGet = new HttpGet(getServerUrlStream()+"/v1/events?accountIds="+getOandaAccount());
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+secret));

        System.out.println("Executing request: " + httpGet.getRequestLine());

        HttpResponse resp = null;
        try {
            resp = httpStream.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                InputStream stream = entity.getContent();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                while ((line = br.readLine()) != null) {
                    log.info("got event line: "+line);
                    if(stopReceiving) break;
                }

                EntityUtils.consume(entity);

//                Object obj = JSONValue.parse(line);
//                JSONObject tick = (JSONObject) obj;

//                    JSONObject values = (JSONObject)tick.get("tick");


            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(resp!=null && resp.getEntity()!=null) {
                try {
                    EntityUtils.consume(resp.getEntity());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    public void logon() {
        eventStreamThread.start();
    }

    public void shutdown() {
        stopReceiving=true;
    }

    public void setReceiptConsumer(IConsumeReceipt _receiptConsumer) {
        this.receiptConsumer = _receiptConsumer;
    }

    public FXOrders(IProvideProperties _prop) {
        prop=_prop;
        httpPoll = new DefaultHttpClient();
        httpStream = new DefaultHttpClient();
        oandaId2OrderMap = new ConcurrentHashMap<String, OrderNew>();
        orderId2OandaIdMap = new ConcurrentHashMap<String, String>();

        receiptConsumer = new IConsumeReceipt() {
            @Override
            public void onReceipt(Receipt receipt) {}
        };

        stopReceiving=false;
        eventStreamThread = new Thread(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private String getOandaAccount() {
        return prop.get("externalAccount");
    }

    private String getSecret() {
        return prop.get("secret");
    }

    private String getServerUrlApi() {
        return prop.get("serverUrlApi");
    }

    private String getServerUrlStream() {
        return prop.get("serverUrlStream");
    }

    private String getUserName() {
        return prop.get("userName");
    }

    private String retrieve(String request) {
        HttpUriRequest httpGet = new HttpGet(getServerUrlApi()+request);
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+getSecret()));

        HttpResponse resp = null;
        try {
            resp = httpPoll.execute(httpGet);

            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                InputStream stream = entity.getContent();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String all = "";
                while ((line = br.readLine()) != null) {
                    all += line;
                }
                stream.close();
                EntityUtils.consume(resp.getEntity());
                return all;
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        try {
            EntityUtils.consume(resp.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private IProvideProperties prop;
    private DefaultHttpClient httpPoll;
    private DefaultHttpClient httpStream;
    private String secret;
    private String serverUrl;
    private String username;
    private ConcurrentHashMap<String, OrderNew> oandaId2OrderMap;
    private ConcurrentHashMap<String, String> orderId2OandaIdMap;
    private IConsumeReceipt receiptConsumer;
    private Thread eventStreamThread;
    private boolean stopReceiving;

    public static void main(String[]args) throws IOException {

        String configFilename = Tool.getPersonalConfigFilename("config/OandaConnection");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        FXOrders fx = new FXOrders(prop);

        fx.logon();


        OrderNew order = OrderNew.create()
                .withBookSide(BookSide.BID)
                .withInternalAccount("test")
                .withSize(Decimal.ONE)
                .withLimit(Decimal.fromString("1.331"))
                .withProductId("OANDA_EURUSD")
                ;
        fx.sendOrderNew(order);

        System.out.println("Order sent. Press enter to continue");
        System.in.read();

        fx.sendOrderCancel(order.createCancelOrder());

        System.out.println("Order canceled. Press enter to continue");
        System.in.read();


        fx.getAccounts();
        fx.getOrders();

    }

} // class
