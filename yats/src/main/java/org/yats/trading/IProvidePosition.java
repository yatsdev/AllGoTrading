package org.yats.trading;

import java.util.Collection;

public interface IProvidePosition {
    public AccountPosition getAccountPosition(PositionRequest positionRequest);
    public IProvidePosition getAllPositionsForOneAccount(String account);
    public Collection<AccountPosition> getAllPositions();
    public Position getValueForAllPositions(RateConverter converter, String targetProductId);
    public Position getValueForAccount(RateConverter converter, String accountId, String targetProductId);
    public Position getValueForAccountProduct(RateConverter converter, PositionRequest request, String targetProductId);
}
