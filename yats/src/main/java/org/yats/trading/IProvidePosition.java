package org.yats.trading;

import org.yats.common.Decimal;

public interface IProvidePosition {
    public Decimal getPosition(PositionRequest positionRequest);
}
