package org.yats.connectivity.oandarest;

import com.oanda.fxtrade.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.Tool;
import org.yats.trading.*;

import java.util.Vector;

public class OandaApi implements IProvidePriceFeed, ISendOrder {

    final Logger log = LoggerFactory.getLogger(OandaApi.class);


    @Override
    public void subscribe(String productId, IConsumeMarketData consumer) {

    }

    @Override
    public void sendOrderNew(OrderNew order) {
        String oandaSymbol = prop.get(order.getProductId());
        FXPair pair = API.createFXPair(oandaSymbol);

        LimitOrder o = API.createLimitOrder();
        o.setPair(pair);
        o.setUnits(order.getSize().toInt());
        o.setPrice(order.getLimit().toDouble());

        try {
            account.execute(o);

        } catch (OAException e) {
            log.error(e.getMessage());
            Receipt r = order.createReceiptDefault()
                    .withRejectReason("oanda exception: "+e.getMessage())
                    .withEndState(true)
                    ;
            receiptConsumer.onReceipt(r);
        }

    }

    @Override
    public void sendOrderCancel(OrderCancel orderCancel) {

    }

    public void logon() {

        String user = prop.get("username");
        String pw = prop.get("password");
        String extAccount = prop.get("externalAccount");
        try {
            while (true) {
                try {
                    fxclient.setTimeout(10);
                    fxclient.setWithRateThread(true);
                    fxclient.login(user,pw,"OandaAPI session");
                    Vector<? extends Account> accounts = fxclient.getUser().getAccounts();
                    account=null;
                    for(Account a : accounts) {
                        String name = a.getAccountName();
                        if(name.compareTo(extAccount)==0) {
                            account = a;
                            break;
                        }
                    }
                    if(account==null) {
                        log.error("Could not find account "+extAccount);
                        fxclient.logout();
                        System.exit(-1);
                    }

                    eventManager = account.getEventManager();
                    FXAccountEvent event = new FXAccountEvent() {
                        @Override
                        public void handle(FXEventInfo fxEventInfo, FXEventManager fxEventManager) {
                        }
                    };

                    eventManager.add(event);
                    break;
                }
                catch (OAException oe) {
                    log.error(oe.getMessage());
                }
                Tool.sleepFor(5000);
            }
            User me = fxclient.getUser();
            System.out.println("name=" + me.getName());
            System.out.println("email=" + me.getEmail());

        }
        catch (SessionException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    public void shutdown() {
        fxclient.logout();
    }

    public void setProductProvider(IProvideProduct productProvider) {
        this.productProvider = productProvider;
    }

    public IProvideProduct getProductProvider() {
        return productProvider;
    }

    public void setReceiptConsumer(IConsumeReceipt receiptConsumer) {
        this.receiptConsumer = receiptConsumer;
    }

    public OandaApi(IProvideProperties _prop) {
        prop=_prop;
        fxclient = API.createFXGame();
        account=null;
        receiptConsumer=null;
        productProvider=null;
        eventManager=null;
    }

    private IProvideProperties prop;
    private IProvideProduct productProvider;
    private FXClient fxclient;
    private IConsumeReceipt receiptConsumer;
    private Account account;
    private FXEventManager eventManager;

} // class
