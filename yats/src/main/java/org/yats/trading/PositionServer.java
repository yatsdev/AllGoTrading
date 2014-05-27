package org.yats.trading;

import org.yats.common.Decimal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class PositionServer implements IConsumeReceipt, IProvidePosition, IProvideProfit{

    public PositionServer() {
        numberOfReceipts = 0;
        positionSnapshot = new PositionSnapshot();
    }

    @Override
    public void onReceipt(Receipt receipt) {
        if (receipt.isRejection()) return;
        if (!receipt.isTrade()) return;
        numberOfReceipts++;
        AccountPosition positionChange = receipt.toAccountPosition();
        positionSnapshot.add(positionChange);
    }

    @Override
    public Decimal getInternalAccountProfitForProduct(String internalAccount, String productId) {
        throw new NotImplementedException();
    }

    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
        return new AccountPosition("pid", "account", Decimal.ZERO);
    }

    public Position getPositionForAllAccounts(String productId) {
        return new Position("pid", Decimal.ZERO);
    }

    public List<AccountPosition> getAllPositionsForOneAccount(String account) {
        return new ArrayList<AccountPosition>();
    }

    public int getNumberOfReceipts() {
        return numberOfReceipts;
    }


    public void addPositionSnapshot(PositionSnapshot newPositionSnapshot) {
          positionSnapshot.add(newPositionSnapshot);
    }

    private int numberOfReceipts;
    private PositionSnapshot positionSnapshot;

}
