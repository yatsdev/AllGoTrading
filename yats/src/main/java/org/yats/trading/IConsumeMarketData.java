package org.yats.trading;

import org.yats.common.UniqueId;

public interface IConsumeMarketData {
    void onMarketData(MarketData marketData);
    UniqueId getConsumerId();
}
