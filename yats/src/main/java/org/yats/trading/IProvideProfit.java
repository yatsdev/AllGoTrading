package org.yats.trading;

public interface IProvideProfit {
    public Position getValueForAccountProduct(PositionRequest request, String targetProductId);
}
