package org.yats.trading;

import org.yats.common.UniqueId;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
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
import org.yats.common.Decimal;
import org.yats.common.Tool;
import org.yats.messagebus.Config;
import org.yats.messagebus.Sender;
import org.yats.messagebus.messages.MarketDataMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.concurrent.ConcurrentHashMap;

public class RateConverter implements IConsumeMarketData {


    public Position convert(Position position, String targetProductId) {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {

            HttpUriRequest httpGet = new HttpGet("https://stream-fxpractice.oanda.com/v1/prices?accountId=3173292&instruments="+targetProductId);
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer 37e6eba904c5a0599c364647b5bb39ed-49b4e985887799ca2a4fb4724b5ebda4"));

            System.out.println("Executing request: " + httpGet.getRequestLine());

            HttpResponse resp = null;
            try {
                resp = httpClient.execute(httpGet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
                InputStream stream = null;
                try {
                    stream = entity.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                try {
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



                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // print error message
                String responseString = null;
                try {
                    responseString = EntityUtils.toString(entity, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(responseString);
            }

        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        throw new NotImplementedException();
//        return new Position(targetProductId, Decimal.ZERO);
    }

    @Override
    public void onMarketData(MarketData marketData) {
        rates.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    public RateConverter(IProvideProduct p) {
        products = p;
        rates = new ConcurrentHashMap<String, MarketData>();
    }


    ConcurrentHashMap<String, MarketData> rates;
    IProvideProduct products;


} // class
