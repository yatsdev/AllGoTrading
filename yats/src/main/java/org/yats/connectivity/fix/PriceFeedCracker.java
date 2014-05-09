package org.yats.connectivity.fix;

import org.yats.common.UniqueId;
import org.yats.trading.IConsumeMarketData;
import org.yats.trading.MarketData;
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

            double bid = MDEntryPx.getValue();
            double bidSize = MDEntrySize.getValue();

            message.getGroup(2, group);
            group.get(MDEntryType);
            group.get(MDEntrySize);
            group.get(MDEntryPx);
            double ask = MDEntryPx.getValue();
            double askSize = MDEntrySize.getValue();

            MarketData m = new MarketData(DateTime.now(DateTimeZone.UTC),productId,bid,ask,bidSize,askSize);
//        log.debug("FIX: "+m.toString());
            marketDataConsumer.onMarketData(m);

        } catch(FieldNotFound e) {
            log.debug("Error parsing market data! "+e.getMessage());
        }
	}

    public void setMarketDataConsumer(IConsumeMarketData marketDataConsumer) {
        this.marketDataConsumer = marketDataConsumer;
    }

    public PriceFeedCracker() {
        this.marketDataConsumer = new MarketDataConsumerDummy();
    }

    private IConsumeMarketData marketDataConsumer;

    private class MarketDataConsumerDummy implements IConsumeMarketData {
        private MarketDataConsumerDummy() {
            id = UniqueId.create();
        }
        @Override
        public void onMarketData(MarketData marketData) {
            throw new RuntimeException("MarketDataConsumerDummy can not handle market data!");
        }
        @Override
        public UniqueId getConsumerId() {
            return id;
        }
        private UniqueId id;
    }
}
