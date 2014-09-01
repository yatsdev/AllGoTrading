package org.yats.connectivity.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.*;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.messagebus.*;
import org.yats.messagebus.messages.PriceDataMsg;
import org.yats.messagebus.messages.SubscriptionMsg;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;

import java.util.concurrent.ConcurrentHashMap;

public class LastPriceServer implements IConsumePriceData, IAmCalledBack {

    final Logger log = LoggerFactory.getLogger(LastPriceServer.class);

    @Override
    public void onPriceData(PriceData priceData) {
        cache.put(priceData.getProductId(), priceData);
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
                PriceDataMsg d = PriceDataMsg.createFrom(cache.get(productId));
                senderPriceDataMsg.publish(d.getTopic(), d);
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
        cache = new ConcurrentHashMap<String, PriceData>();
        readCacheFromDisk();
        Config config =  Config.fromProperties(prop);
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setPriceDataConsumer(this);

        receiverSubscription = new BufferingReceiver<SubscriptionMsg>(SubscriptionMsg.class,
                config.getExchangeSubscription(),
                config.getTopicSubscriptions(),
                config.getServerIP());
        receiverSubscription.setObserver(this);
        receiverSubscription.start();
        senderPriceDataMsg = new Sender<PriceDataMsg>(config.getExchangePriceData(), config.getServerIP());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void writeCacheToDisk() {
        StringBuilder b = new StringBuilder();
        Serializer<PriceDataMsg> serializer = new Serializer<PriceDataMsg>();
        for(String key : cache.keySet()) {
            PriceData d = cache.get(key);
            PriceDataMsg m = PriceDataMsg.createFrom(d);
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
        Deserializer<PriceDataMsg> deserializer = new Deserializer<PriceDataMsg>(PriceDataMsg.class);
        String wholeFile = FileTool.readFromTextFile(cacheFilename);
        String[] parts = wholeFile.split("\n");
        for(String s : parts) {
            if(s.length()==0) continue;
            PriceDataMsg m = deserializer.convertFromString(s);
            PriceData d = m.toPriceData();
            cache.put(d.getProductId(), d);
        }
        log.info("Cache read from disk with "+cache.size()+" items.");
    }

    private final String cacheFilename;
    private Sender<PriceDataMsg> senderPriceDataMsg;
    private ConcurrentHashMap<String, PriceData> cache;
    private BufferingReceiver<SubscriptionMsg> receiverSubscription;
    private final boolean shutdown;
    private final StrategyToBusConnection strategyToBusConnection;

    private final IProvideProperties prop;
}
