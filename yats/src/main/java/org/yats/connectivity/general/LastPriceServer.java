package org.yats.connectivity.general;

import com.pretty_tools.dde.client.DDEClientConversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.connectivity.messagebus.MarketToBusConnection;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.*;
import org.yats.messagebus.messages.MarketDataMsg;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.MarketData;

import java.util.concurrent.ConcurrentHashMap;

public class LastPriceServer implements IConsumeMarketData, IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(LastPriceServer.class);

    @Override
    public void onMarketData(MarketData marketData) {
        cache.put(marketData.getProductId(), marketData);
    }

    @Override
    public UniqueId getConsumerId() {
        return UniqueId.create();
    }

    @Override
    public void onCallback() {
        while(receiverSubscription.hasMoreMessages()) {
            String productId = receiverSubscription.get().productId;
            if(cache.containsKey(productId)) {
                MarketDataMsg d = MarketDataMsg.createFrom(cache.get(productId));
                senderMarketDataMsg.publish(d.getTopic(), d);
            }
        }
    }

    public void go() {
    }

    public void close() {
        writeCacheToDisk();
        strategyToBusConnection.close();
        receiverSubscription.close();
    }



    public LastPriceServer(IProvideProperties _prop)
    {
        shutdown=false;
        prop = _prop;
        cacheFilename = prop.get("cacheFilename");
        cache = new ConcurrentHashMap<String, MarketData>();
        readCacheFromDisk();
        Config config =  Config.fromProperties(prop);
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setMarketDataConsumer(this);

        receiverSubscription = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                config.getExchangeSubscription(),
                config.getTopicSubscriptions(),
                config.getServerIP());
        receiverSubscription.setObserver(this);
        receiverSubscription.start();
        senderMarketDataMsg = new Sender<MarketDataMsg>(config.getExchangeMarketData(), config.getServerIP());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void writeCacheToDisk() {
        StringBuilder b = new StringBuilder();
        Serializer<MarketDataMsg> serializer = new Serializer<MarketDataMsg>();
        for(String key : cache.keySet()) {
            MarketData d = cache.get(key);
            MarketDataMsg m = MarketDataMsg.createFrom(d);
            String s = serializer.convertToString(m);
            b.append(s).append("\n");
        }
        FileTool.writeToTextFile(cacheFilename, b.toString(), false);
        log.info("Cache written to disk with "+cache.size()+" items.");
    }

    private void readCacheFromDisk() {
        if(!FileTool.exists(cacheFilename)) return;
        cache.clear();
        StringBuilder b = new StringBuilder();
        Deserializer<MarketDataMsg> deserializer = new Deserializer<MarketDataMsg>(MarketDataMsg.class);
        String wholeFile = FileTool.readFromTextFile(cacheFilename);
        String[] parts = wholeFile.split("\n");
        for(String s : parts) {
            if(s.length()==0) continue;
            MarketDataMsg m = deserializer.convertFromString(s);
            MarketData d = m.toMarketData();
            cache.put(d.getProductId(), d);
        }
        log.info("Cache read from disk with "+cache.size()+" items.");
    }

    private final String cacheFilename;
    private Sender<MarketDataMsg> senderMarketDataMsg;
    private ConcurrentHashMap<String, MarketData> cache;
    private BufferingReceiver<SubscriptionMsg> receiverSubscription;
    private final boolean shutdown;
    private final StrategyToBusConnection strategyToBusConnection;

    private final IProvideProperties prop;
}
