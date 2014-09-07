package org.yats.connectivity.fix;

import org.yats.common.Decimal;
import org.yats.common.UniqueId;
import org.yats.trading.IConsumePriceData;
import org.yats.trading.PriceData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.*;
import quickfix.fix42.Logon;
import quickfix.fix42.MessageCracker;

public class PriceFeedCracker extends MessageCracker implements Application {
    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(PriceFeedCracker.class);

	public void onCreate(SessionID sessionID) {
	}

	public void onLogon(SessionID sessionID) {

	}

	public void onLogout(SessionID sessionID) {

	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID) {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
    {
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
    {

		try {
			crack(message, sessionID);

		} catch (Exception e) {
            e.printStackTrace();
		}

	}

	@Override
	public void onMessage(Logon message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		System.out.println("Inside Logon Message");
		super.onMessage(message, sessionID);
	}

	public void onMessage(quickfix.fix42.MarketDataSnapshotFullRefresh message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue
    {

		NoMDEntries noMDEntries = new NoMDEntries();

		message.getField(noMDEntries);

		quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries group = new quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries();
		MDEntryType MDEntryType = new MDEntryType();
		MDEntryPx MDEntryPx = new MDEntryPx();
		MDEntrySize MDEntrySize = new MDEntrySize();

        String symbol = message.get(new Symbol()).getValue();
        String productId = message.get(new SecurityID()).getValue();

        try {
            message.getGroup(1, group);
            group.get(MDEntryType);
            group.get(MDEntrySize);
            group.get(MDEntryPx);

            Decimal bid = new Decimal(MDEntryPx.getValue());
            Decimal bidSize = new Decimal(MDEntrySize.getValue());

            message.getGroup(2, group);
            group.get(MDEntryType);
            group.get(MDEntrySize);
            group.get(MDEntryPx);
            Decimal ask = new Decimal(MDEntryPx.getValue());
            Decimal askSize = new Decimal(MDEntrySize.getValue());

            Decimal last = bid.add(ask).divide(Decimal.fromString("2"));

            PriceData m = new PriceData(DateTime.now(DateTimeZone.UTC),productId
                    ,bid,ask,last
                    ,bidSize,askSize, Decimal.ONE);
//        log.debug("FIX: "+m.toString());
            priceDataConsumer.onPriceData(m);

        } catch(FieldNotFound e) {
            log.debug("Error parsing market data! "+e.getMessage());
        }
	}

    public void setPriceDataConsumer(IConsumePriceData priceDataConsumer) {
        this.priceDataConsumer = priceDataConsumer;
    }

    public PriceFeedCracker() {
        this.priceDataConsumer = new PriceDataConsumerDummy();
    }

    private IConsumePriceData priceDataConsumer;

    private class PriceDataConsumerDummy implements IConsumePriceData {
        private PriceDataConsumerDummy() {
            id = UniqueId.create();
        }
        @Override
        public void onPriceData(PriceData priceData) {
            throw new RuntimeException("PriceDataConsumerDummy can not handle market data!");
        }
        @Override
        public UniqueId getConsumerId() {
            return id;
        }
        private UniqueId id;
    }
}
