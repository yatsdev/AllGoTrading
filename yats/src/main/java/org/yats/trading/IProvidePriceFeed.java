package org.yats.trading;

public interface IProvidePriceFeed {
    void subscribe(String productId, IConsumeMarketData consumer);
}
