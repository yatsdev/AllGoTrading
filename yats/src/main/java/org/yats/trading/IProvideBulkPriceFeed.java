package org.yats.trading;

public interface IProvideBulkPriceFeed {
    void subscribeBulk(String productId, IConsumeBulkPriceData consumer);
}
