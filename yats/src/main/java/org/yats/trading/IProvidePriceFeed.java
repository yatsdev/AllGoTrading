package org.yats.trading;

public interface IProvidePriceFeed {
    void subscribe(Product p, IConsumeMarketData consumer);
}
