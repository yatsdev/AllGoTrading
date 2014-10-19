package org.yats.trading;

import java.util.Collection;

public interface IConsumeBulkPriceData {
    void onBulkPriceData(Collection<? extends PriceData> data);
}
