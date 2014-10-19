package org.yats.trading;

import org.yats.common.UniqueId;

public interface IConsumePriceData {
    void onPriceData(PriceData priceData);
    UniqueId getConsumerId();
    // todo: get rid of id. it cant be responsibility of consumer to provide unique id for server
}
