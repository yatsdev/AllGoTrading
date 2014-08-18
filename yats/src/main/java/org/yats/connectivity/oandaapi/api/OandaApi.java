/*

If you do not plan to send orders to Oanda exclude "oandaapi" recursively from compilation
(Settings->Compiler->Excludes in IntelliJ IDEA)

To use the Oanda connection to send orders you need to
- apply for an account with Oanda
- obtain the Oanda_fxtrade Java library
- include it in the library path
- duplicate the configuration template file and add your Oanda user/password

 */

package org.yats.connectivity.oandaapi.api;

import com.oanda.fxtrade.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
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

        LimitOrder oandaOrder = API.createLimitOrder();
        oandaOrder.setPair(pair);
        int size = order.getSize().toInt();
        double price = order.getLimit().toDouble();
        boolean buy = order.isForBookSide(BookSide.BID);
        if(buy) {
            oandaOrder.setLowPriceLimit(0.000001);
            oandaOrder.setHighPriceLimit(1.00001*price);
        } else
        {
            oandaOrder.setLowPriceLimit(0.99999*price);
            oandaOrder.setHighPriceLimit(100*price);
            size = -size;
        }
        oandaOrder.setUnits(size);
        oandaOrder.setPrice(price);

        long tempDuration = 24 * 60 * 60 + fxclient.getServerTime(); // 24h duration
        oandaOrder.setExpiry(tempDuration);

        try {
            account.execute(oandaOrder);

            LimitOrderEvent event = new LimitOrderEvent(order, oandaOrder, receiptConsumer,account);
            eventManager.add(event);

        } catch (OAException e) {
            log.error(e.getMessage());
            Receipt r = order.createReceiptDefault()
                    .withRejectReason("oanda exception: "+e.getMessage())
                    .withEndState(true)
                    ;
            receiptConsumer.onReceipt(r);
            return;
        }
        Receipt r = order.createReceiptDefault()
                .withEndState(false)
                ;
//        receiptConsumer.onReceipt(r);
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


class LimitOrderEvent extends FXAccountEvent
{

    final Logger log = LoggerFactory.getLogger(LimitOrderEvent.class);

    @Override
    public void handle(FXEventInfo fxEventInfo, FXEventManager EM) {
        try
        {
            int oandaOrderId = oandaOrder.getTransactionNumber();
            LimitOrder oldOrder = account.getOrderWithId(oandaOrderId);
            if (oldOrder == null)
            {
                log.info("Order closed: "
                        + order.getOrderId() + " oandaId:"
                        + oandaOrder.getTransactionNumber());
                EM.remove(this);
                Receipt r = order.createReceiptDefault()
                        .withEndState(true)
                        .withCurrentTradedSize(Decimal.ZERO)
                        .withTotalTradedSize(Decimal.ZERO)
                        ;
                receiptConsumer.onReceipt(r);
                return;
            }
        }
        catch (OAException err)
        {
            log.error("Exception handling order "
                    + order.getOrderId() + " oandaId:"
                    + oandaOrder.getTransactionNumber()
                    + " " + err.getMessage());
            EM.remove(this);
        }
        Receipt r = order.createReceiptDefault()
                .withEndState(true)
                .withCurrentTradedSize(order.getSize())
                .withTotalTradedSize(order.getSize())
                ;
        receiptConsumer.onReceipt(r);
    }


    LimitOrderEvent(OrderNew _order, LimitOrder _oandaOrder, IConsumeReceipt _receiptConsumer, Account _account) {
//        super(""+_oandaOrder.getTransactionNumber());
        super();
        order = _order;
        oandaOrder=_oandaOrder;
        receiptConsumer=_receiptConsumer;
        account = _account;
    }

    private Account account;
    private OrderNew order;
    private LimitOrder oandaOrder;
    private IConsumeReceipt receiptConsumer;






}

