package org.yats.connectivity.fix;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.BookSide;
import org.yats.trading.IConsumeReceipt;
import org.yats.trading.Receipt;
import quickfix.*;
import quickfix.fix42.*;
import quickfix.fix42.MessageCracker;




public class OrderResponseCracker extends MessageCracker implements Application {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(OrderResponseCracker.class);


    public void onCreate(SessionID sessionID) {
	}

	public void onLogon(SessionID sessionID) {

	}

	public void onLogout(SessionID sessionID) {

	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID)
			throws DoNotSend {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {

	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {

		try {
			crack(message, sessionID);

		} catch (Exception e) {
            log.error(e.getMessage());
		}

	}

	@Override
	public void onMessage(Logon message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		System.out.println("Inside Logon Message");
		super.onMessage(message, sessionID);
	}

//	public void onMessage(quickfix.fix42.NewOrderSingle order,
//			SessionID sessionID) {
//
//		log.info("NOT IMPLEMENTED YET:::::::::::::::NEWORDERSINGLE!!");
//		try {
//			OrderQty orderQty = order.getOrderQty();
//			System.out.println(orderQty.toString());
//			Price price = order.getPrice();
//			System.out.println(price.toString());
//		} catch (FieldNotFound e) {
//			e.printStackTrace();
//		}
//
//	}

	public void onMessage(ExecutionReport report, SessionID sessionID)
    {
        String cancelTypes = "4C";
		try {
            if(!isReportInteresting(report.getExecType())) {
                log.debug("discarding execution report: " + report);
                return;
            }

            log.debug("execution report received: " + report);

            Receipt r = new Receipt();
            r.setTimestamp(DateTime.now(DateTimeZone.UTC));

            r.setExternalAccount(report.getAccount().getValue());
            r.setProductId(report.getSecurityID().getValue());
            r.setPrice(new Decimal(report.getPrice().getValue()));
            r.setResidualSize(new Decimal(report.getLeavesQty().getValue()));
            r.setCurrentTradedSize(new Decimal(report.getLastShares().getValue()));
            r.setOrderId(UniqueId.createFromString(report.getClOrdID().getValue()));
            if(cancelTypes.indexOf(report.getExecType().getValue())>=0) { // cancel
                r.setOrderId(UniqueId.createFromString(report.getOrigClOrdID().getValue()));
            }
            if(report.getExecType().getValue()=='8') { // rejected
                r.setRejectReason(report.getOrdRejReason().toString());
            }

            r.setBookSide(BookSide.ASK);
            if(report.getSide().getValue()=='1') r.setBookSide(BookSide.BID);

            if(r.getResidualSize().isEqualTo(Decimal.ZERO)) r.setEndState(true);

            r.setEndState(isInEndState(report.getOrdStatus().getValue()));

            receiptConsumer.onReceipt(r);

		} catch (FieldNotFound e) {
            log.debug(e.getMessage());
		}

	}

    @Override
    public void onMessage(OrderCancelReject report, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        try {
            log.debug("cancel reject received: " + report);

            Receipt r = Receipt.create()
                    .withTimestamp(DateTime.now(DateTimeZone.UTC))
                    .withOrderId(UniqueId.createFromString(report.getOrigClOrdID().getValue()))
                    .withEndState(isInEndState(report.getOrdStatus().getValue()))
                    .withRejectReason(report.getText().getValue())
                    ;

            receiptConsumer.onReceipt(r);

        } catch (FieldNotFound e) {
            log.debug(e.getMessage());
        }
    }

    public void onMessage(Reject fixSessionLevelRejection, SessionID sessionID) {
		quickfix.fix42.Reject reject = new quickfix.fix42.Reject();
		log.info("NOT IMPLEMENTED YET::::::::::::::: FIX rejection!!");
        String reason = "onMessage(Reject fixSessionLevelRejection, SessionID sessionID)";
        try {
            reason = "FIX session level rejection:" + reject.getSessionRejectReason().toString();
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
        }
        log.info(reason);
        throw new RuntimeException(reason);
	}

    void setReceiptConsumer(IConsumeReceipt r)
    {
        receiptConsumer = r;
    }


    private boolean isReportInteresting(quickfix.field.ExecType type)
    {
        /* http://www.onixs.biz/fix-dictionary/4.2/msgType_8_8.html
            0 = New
            1 = Partial fill
            2 = Fill
            3 = Done for day
            4 = Canceled
            5 = Replace
            6 = Pending Cancel (e.g. result of Order Cancel Request <F>)
            7 = Stopped
            8 = Rejected
            9 = Suspended
            A = Pending New
            B = Calculated
            C = Expired  -> canceled
            D = Restated (ExecutionRpt sent unsolicited by sellside, with ExecRestatementReason <378> set)
            E = Pending Replace (e.g. result of Order Cancel/Replace Request <G>)
         */
        String interestingTypes = "012458C";
        return interestingTypes.indexOf(type.getValue()) >= 0;
    }

    private boolean isInEndState(char fixOrderStatus) {
        String fixCodesForEndState = "23478C";
        return fixCodesForEndState.indexOf(fixOrderStatus) >=0;
    }



    private IConsumeReceipt receiptConsumer;

}
