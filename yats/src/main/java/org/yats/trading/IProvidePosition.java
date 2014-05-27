package org.yats.trading;

public interface IProvidePosition {
    public AccountPosition getAccountPosition(PositionRequest positionRequest);
}
