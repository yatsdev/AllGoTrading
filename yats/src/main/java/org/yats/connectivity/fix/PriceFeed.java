package org.yats.connectivity.fix;

import org.yats.trading.*;
import org.yats.trading.Product;
import quickfix.*;
import quickfix.field.*;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class PriceFeed implements IProvidePriceFeed {



    public synchronized void logon()
    {
        if (!initiatorStarted) {
            try {
                initiator.start();
            } catch (ConfigError configError) {
                configError.printStackTrace();
                throw new RuntimeException(configError.getMessage());
            }
            initiatorStarted = true;
         } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    @Override
    public void subscribe(String productId, IConsumeMarketData consumer)
    {
        Random generator = new Random();
        int r = generator.nextInt();
        quickfix.fix42.MarketDataRequest marketDataRequest = new quickfix.fix42.MarketDataRequest(
                new MDReqID(Integer.toString(r * r)),
                new SubscriptionRequestType('1'), new MarketDepth(1));

        quickfix.fix42.MarketDataRequest.NoRelatedSym group = new quickfix.fix42.MarketDataRequest.NoRelatedSym();

        SessionID sessionId = initiator.getSessions().get(0);

        Product p = productProvider.getProductForProductId(productId);
        group.set(new Symbol(p.getSymbol()));
        group.set(new SecurityID(p.getProductId()));
        group.set(new SecurityExchange(p.getExchange()));

        marketDataRequest.setField(new MDUpdateType(0));
        marketDataRequest.setField(new NoMDEntryTypes(0));
        marketDataRequest.addGroup(group);

        try {
            application.setMarketDataConsumer(consumer);
            Session.sendToTarget(marketDataRequest, sessionId);
        } catch (SessionNotFound e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }



    public static PriceFeed create()
    {
        String configStringDefault = "[default]\n" +
                "FileStorePath=data\n" +
                "ConnectionType=initiator\n" +
                "SenderCompID=HIQ_PRICE\n" +
                "TargetCompID=HIQFIX\n" +
                "SocketConnectHost=46.244.8.46\n" +
                "StartTime=00:00:00\n" +
                "EndTime=00:00:00\n" +
                "HeartBtInt=30\n" +
                "ReconnectInterval=5\n" +
                "\n" +
                "[session]\n" +
                "BeginString=FIX.4.2\n" +
                "SocketConnectPort=5001";

        return createFromConfigString(configStringDefault);
    }

    public static PriceFeed createFromConfigFile(String pathToConfigFile)
    {
        try {
            String configAsString = new Scanner(new File(pathToConfigFile)).useDelimiter("\\Z").next();
            return createFromConfigString(configAsString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static PriceFeed createFromConfigString(String config)
    {
        try {
            InputStream inputStream = new ByteArrayInputStream(config.getBytes());
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();
            return new PriceFeed(settings);
        } catch (ConfigError configError) {
            configError.printStackTrace();
            throw new RuntimeException(configError.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public PriceFeed(SessionSettings settings) throws Exception {

        productProvider = new ProductList();
        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));

        application = new PriceFeedCracker();

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
//        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        LogFactory logFactory = new ScreenLogFactory(false, false, false, false);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory,
                settings, logFactory, messageFactory);

    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    private boolean initiatorStarted = false;
    private static Initiator initiator = null;
    private PriceFeedCracker application;
    private IProvideProduct productProvider;

}