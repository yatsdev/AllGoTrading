package org.yats.trading;

import java.util.Collection;

public interface IProvidePosition {
    public AccountPosition getAccountPosition(PositionRequest positionRequest);
    public IProvidePosition getAllPositionsForOneAccount(String account);
    public Collection<AccountPosition> getAllPositions();
    public Position getValueForAccount(String targetProductId, String accountId);
    public Position getValueForAllPositions(String targetProductId);
    public Position getValueForAccountProduct(String targetProductId, PositionRequest request);
}
