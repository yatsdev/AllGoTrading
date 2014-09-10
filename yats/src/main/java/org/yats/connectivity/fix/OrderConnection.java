package org.yats.connectivity.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.PropertiesReader;
import org.yats.common.UniqueId;
import org.yats.trading.*;
import org.yats.trading.Product;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

public class OrderConnection implements ISendOrder {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(OrderResponseCracker.class);

    private static final String ROUTE = "RouteAGT";

    public synchronized void logon() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;

            } catch (Exception e) {
                log.error(e.getMessage());
                System.exit(-1);
            }
        } else {
            for (SessionID sessionId : initiator.getSessions()) {
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public static OrderConnection create()
    {
        String configStringDefault = "[default]\n" +
                "FileStorePath=data\n" +
                "ConnectionType=initiator\n" +
                "SenderCompID=HIQ_ORDER\n" +
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

    public static OrderConnection createFromConfigFile(String pathToConfigFile)
    {
        try {
            String configAsString = new Scanner(new File(pathToConfigFile)).useDelimiter("\\Z").next();
            OrderConnection o = createFromConfigString(configAsString);
            PropertiesReader r = PropertiesReader.createFromConfigFile(pathToConfigFile);
            o.setExternalAccount(r.get("externalAccount"));
            return o;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static OrderConnection createFromConfigString(String config)
    {
        try {
            InputStream inputStream = new ByteArrayInputStream(config.getBytes());
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();
            return new OrderConnection(settings);
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


    public OrderConnection(SessionSettings settings) throws Exception
    {
        externalAccount="n/a";
        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true"));
        orderResponseCracker = new OrderResponseCracker();
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();
        initiator = new SocketInitiator(orderResponseCracker, messageStoreFactory, settings, logFactory, messageFactory);
    }

    @Override
    public void sendOrderNew(OrderNew orderNew)
    {
        if(!isCorrectRoute(orderNew.getProductId())) return;
		NewOrderSingle fixOrder = createFixNewOrder(orderNew);
        SessionID sessionId = initiator.getSessions().get(0);
        try {
            Session.sendToTarget(fixOrder, sessionId); // assumed to be threadsafe!
            log.debug("Sent order new: "+orderNew.toString());
            log.debug("Sent order new as fix: "+fixOrder.toString());
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
            throw new RuntimeException(sessionNotFound.getMessage());
        }
    }

	@Override
    public void sendOrderCancel(OrderCancel orderCancel)
    {
        if(!isCorrectRoute(orderCancel.getProductId())) return;
        OrderCancelRequest fixOrderCancel = createFixCancelOrder(orderCancel);
		SessionID sessionId = initiator.getSessions().get(0);
        try {
            Session.sendToTarget(fixOrderCancel, sessionId); // assumed to be threadsafe!
            log.debug("Send order cancel: " + orderCancel);
            log.debug("Send order cancel as fix: "+fixOrderCancel);
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
            throw new RuntimeException(sessionNotFound.getMessage());
        }
    }


    private boolean isCorrectRoute(String productId) {
        try {
            Product p = productProvider.getProductWith(productId);
            return p.isRoute(ROUTE);
        } catch(TradingExceptions.ItemNotFoundException e) {}
        return false;
    }

    private OrderCancelRequest createFixCancelOrder(OrderCancel orderCancel)
    {
        OrderCancelRequest fixOrder = new OrderCancelRequest();
        fixOrder.set(new TransactTime(new Date(0)));
        fixOrder.set(new OrigClOrdID(orderCancel.getOrderId().toString()));
        fixOrder.set(new ClOrdID(UniqueId.create().toString()));
//        fixOrder.set(new Account(orderCancel.getExternalAccount()));
        fixOrder.set(new Account(externalAccount));
        Product p = productProvider.getProductWith(orderCancel.getProductId());
        fixOrder.set(new Symbol(p.getSymbol()));
        if (orderCancel.getSide().toDirection()>0) {
            fixOrder.set(new Side(Side.BUY));
        } else {
            fixOrder.set(new Side(Side.SELL));
        }
        return fixOrder;
    }

    private NewOrderSingle createFixNewOrder(OrderNew order)
    {
        quickfix.fix42.NewOrderSingle fixOrder = new quickfix.fix42.NewOrderSingle();
        fixOrder.set(new TransactTime(new Date(0)));
        fixOrder.set(new HandlInst('1'));
//        fixOrder.set(new Account(order.getExternalAccount()));
        fixOrder.set(new Account(externalAccount));
        Product p = productProvider.getProductWith(order.getProductId());
        BookSide side = order.getBookSide();
        fixOrder.set(new Symbol(p.getSymbol()));
        fixOrder.set(new SecurityID(p.getProductId()));
        fixOrder.set(new SecurityExchange(p.getExchange()));
        fixOrder.set(new Price(order.getLimit().toBigDecimal()));
        if (side.toDirection()==1) {
            fixOrder.set(new Side(Side.BUY));
        } else {
            fixOrder.set(new Side(Side.SELL));
        }
        fixOrder.set(new OrderQty(order.getSize().toBigDecimal()));
        fixOrder.set(new OrdType(OrdType.LIMIT));
        fixOrder.set(new ClOrdID(order.getOrderId().toString()));
        return fixOrder;
    }

    public void setReceiptConsumer(IConsumeReceipt r)
    {
        orderResponseCracker.setReceiptConsumer(r);
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public void setExternalAccount(String externalAccount) {
        this.externalAccount = externalAccount;
    }

    private OrderResponseCracker orderResponseCracker;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    private IProvideProduct productProvider;
    private String externalAccount;


}