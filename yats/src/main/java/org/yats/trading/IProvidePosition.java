package org.yats.trading;

import java.util.Collection;

public interface IProvidePosition {
    public AccountPosition getAccountPosition(PositionRequest positionRequest);
    public IProvidePosition getAllPositionsForOneAccount(String account);
    public Collection<AccountPosition> values();
}
