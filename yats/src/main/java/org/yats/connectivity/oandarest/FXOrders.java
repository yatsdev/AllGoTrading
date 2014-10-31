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
import org.yats.connectivity.Id2ReceiptMap;
import org.yats.trading.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FXOrders implements ISendOrder, Runnable {

    public static void main(String[]args) throws IOException {

        String configFilename = Tool.getPersonalConfigFilename("config","OandaConnection");
        PropertiesReader prop = PropertiesReader.createFromConfigFile(configFilename);

        FXOrders fx = new FXOrders(prop);

        fx.logon();

        System.out.println("Init completed. Press enter to continue");
        System.in.read();

        OrderNew order = OrderNew.create()
                .withBookSide(BookSide.ASK)
                .withInternalAccount("test")
                .withSize(Decimal.TWO)
                .withLimit(Decimal.fromString("1.3160"))
                .withProductId("OANDA_EURUSD")
                ;
        fx.sendOrderNew(order);

        System.out.println("First order sent. Press enter to continue");
        System.in.read();

        fx.sendOrderNew(order);

        System.out.println("Second order sent. Press enter to continue");
        System.in.read();

        fx.sendOrderCancel(order.createCancelOrder());

        System.out.println("Order canceled. Press enter to continue");
        System.in.read();


        fx.getAccounts();
        fx.getOrders();

        fx.shutdown();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////


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
        String price = orderNew.getLimit().toString();
        String priceMax = orderNew.getLimit().multiply(Decimal.TEN).toString();
        String lowerBound = orderNew.isForBookSide(BookSide.BID) ? "0" : price;
        String upperBound = orderNew.isForBookSide(BookSide.BID) ? price : priceMax;
        nameValuePairs.add(new BasicNameValuePair("units",""+size));
        nameValuePairs.add(new BasicNameValuePair("side", orderNew.getBookSide().toBuySellString()));
        nameValuePairs.add(new BasicNameValuePair("type","limit"));
        nameValuePairs.add(new BasicNameValuePair("price", price));
        nameValuePairs.add(new BasicNameValuePair("lowerBound", lowerBound));
        nameValuePairs.add(new BasicNameValuePair("upperBound", upperBound));

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
            id2ReceiptMap.putOrderId2ExternalIdMapping(orderNew.getOrderId().toString(), oandaOrderId);
            id2ReceiptMap.putReceipt(oandaOrderId, orderNew.createReceiptDefault().withExternalAccount(getOandaAccount()));
            id2ReceiptMap.storeToDisk();
            EntityUtils.consume(response.getEntity());
            Receipt r = orderNew.createReceiptDefault().withEndState(false);
            receiptConsumer.onReceipt(r);
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
    }

    @Override
    public void sendOrderCancel(OrderCancel orderCancel) {
        boolean orderIdKnown = id2ReceiptMap.containsReceiptForOrderId(orderCancel.getOrderId().toString());
        if(!orderIdKnown) {
            String rejection = "No Oanda order id found for "+orderCancel.getOrderId();
            Receipt r = orderCancel.createReceiptDefault()
                    .withRejectReason(rejection)
                    .withEndState(true)
                    ;
            log.error(rejection);
            receiptConsumer.onReceipt(r);
            return;
        }

        String oandaOrderId = id2ReceiptMap.getExternalId(orderCancel.getOrderId().toString());


        String urlString = getServerUrlApi()+"/v1/accounts/"+ getOandaAccount()+"/orders/"+oandaOrderId;
        HttpUriRequest httpGet = new HttpDelete(urlString);
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+getSecret()));

        HttpResponse resp = null;
        try {
            resp = httpPoll.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                log.info("Sent CancelOrder with orderId="+orderCancel.getOrderId()+" oandaId="+oandaOrderId);
//                OrderNew orderNew = oandaId2OrderMap.get(oandaOrderId);
//                Receipt r = orderNew.createReceiptDefault().withEndState(true);
//                orderId2OandaIdMap.remove(orderCancel.getOrderId().toString());
//                oandaId2OrderMap.remove(oandaOrderId);
//                receiptConsumer.onReceipt(r);
            }
            if(entity!=null) EntityUtils.consume(entity);
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



    @Override
    public void run() {
        HttpUriRequest httpGet = new HttpGet(getServerUrlStream()+"/v1/events?accountIds="+getOandaAccount());
        httpGet.setHeader(new BasicHeader("Authorization", "Bearer "+getSecret()));

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
                    if(stopReceiving) break;
                    if(line.contains("{\"heartbeat")) continue;
                    Object obj = JSONValue.parse(line);
                    JSONObject msg = (JSONObject) obj;
//                    log.info(msg.toString());
                    if(!line.startsWith("{\"transaction")) continue;
                    JSONObject values = (JSONObject)msg.get("transaction");

                    String id = values.containsKey("orderId")
                            ? values.get("orderId").toString()
                            : values.get("id").toString();
                    String type = values.get("type").toString();
                    if(!id2ReceiptMap.containsReceiptForExternalId(id)) {
                        log.error("OandaReceipt: got receipt for unknown order: "+msg);
                        continue;
                    }
                    Receipt r  = id2ReceiptMap.getReceiptForExternalId(id);
                    if(type.compareTo("LIMIT_ORDER_CREATE")==0){
                        log.info("OandaReceipt: LIMIT_ORDER_CREATE for "+id);
                    } else
                    if(type.compareTo("ORDER_CANCEL")==0) {
                        log.info("OandaReceipt: ORDER_CANCEL for "+id);
                        r=r.withEndState(true);
                        id2ReceiptMap.remove(r.getOrderId().toString());
                        id2ReceiptMap.storeToDisk();
                    } else
                    if(type.compareTo("ORDER_FILLED")==0) {
                        log.info("OandaReceipt: ORDER_FILLED for "+id);
                        Decimal currentTradedSize = Decimal.fromString(values.get("units").toString());
                        r.setCurrentTradedSize(currentTradedSize);
                        r.setResidualSize(r.getResidualSize().subtract(currentTradedSize));
                        r.setTotalTradedSize(r.getTotalTradedSize().add(currentTradedSize));
                        if(r.isPartialFill()) log.info("Order filled partially: "+r.toString());
                        r.setEndState(!r.isPartialFill());
                        id2ReceiptMap.remove(r.getOrderId().toString());
                        id2ReceiptMap.storeToDisk();
                    } else {
                        log.error("OandaReceipt: Unknown receipt type: "+msg);
                    }
                    receiptConsumer.onReceipt(r);

                } // while

                EntityUtils.consume(entity);
            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println("response: "+responseString);
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
        id2ReceiptMap.storeToDisk();
    }

    public void setReceiptConsumer(IConsumeReceipt _receiptConsumer) {
        this.receiptConsumer = _receiptConsumer;
    }

    public FXOrders(IProvideProperties _prop) {
        prop=_prop;
        httpPoll = new DefaultHttpClient();
        httpStream = new DefaultHttpClient();
        String idFileName = prop.exists("orderStorageFilename") ? prop.get("orderStorageFilename") : "OandaOrderMap";
        id2ReceiptMap = new Id2ReceiptMap(idFileName);
        id2ReceiptMap.readFromDisk();

        receiptConsumer = new IConsumeReceipt() {
            @Override
            public void onReceipt(Receipt receipt) {}
        };

        stopReceiving=false;
        eventStreamThread = new Thread(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////

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
//    private ConcurrentHashMap<String, Receipt> oandaId2Receipt;
//    private ConcurrentHashMap<String, String> orderId2OandaIdMap;
//    private String orderId2OandaIdMapFilename;
//    private String oandaId2OrderMapFilename;
    Id2ReceiptMap id2ReceiptMap;
    private IConsumeReceipt receiptConsumer;
    private Thread eventStreamThread;
    private boolean stopReceiving;



} // class
