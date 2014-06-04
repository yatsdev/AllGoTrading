package org.yats.connectivity.messagebus;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.*;
import org.apache.http.client.methods.*;
//import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FXRates {
    public static void main (String[]args) throws IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {

            HttpUriRequest httpGet = new HttpGet("https://stream-fxpractice.oanda.com/v1/prices?accountId=3173292&instruments=EUR_USD,USD_CAD,USD_JPY");
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer 37e6eba904c5a0599c364647b5bb39ed-49b4e985887799ca2a4fb4724b5ebda4"));

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
                    }
                }
            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);
            }

        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}