package org.yats.trading;

import org.yats.common.UniqueId;

public interface IConsumePriceData {
    void onPriceData(PriceData priceData);
    UniqueId getConsumerId();
}
