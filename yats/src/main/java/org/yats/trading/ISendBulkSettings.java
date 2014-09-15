package org.yats.trading;

import org.yats.common.IProvideProperties;

import java.util.Collection;

public interface ISendBulkSettings {
    void sendBulkSettings(Collection<IProvideProperties> all);
}
