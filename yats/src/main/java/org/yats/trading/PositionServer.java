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
        return positionSnapshot.getAccountPosition(positionRequest);
    }

    public Position getPositionForAllAccounts(String productId) {
        return positionSnapshot.getPositionForAllAccounts(productId);
    }

    public List<AccountPosition> getAllPositionsForOneAccount(String account) {
        return new ArrayList<AccountPosition>();
    }

    public int getNumberOfReceipts() {
        return numberOfReceipts;
    }

    public int getNumberOfPositions() {
        return positionSnapshot.size();
    }

    public void addPositionSnapshot(PositionSnapshot newPositionSnapshot) {
          positionSnapshot.add(newPositionSnapshot);
    }

    public PositionSnapshot getPositionSnapshot() {
        return positionSnapshot;
    }

    private int numberOfReceipts;
    private PositionSnapshot positionSnapshot;

    public boolean isEmpty() {
        return positionSnapshot.size()==0;
    }

    public void setPositionSnapshot(PositionSnapshot positionSnapshot) {
        this.positionSnapshot = positionSnapshot;
    }

    public void clearPositions() {
        positionSnapshot = new PositionSnapshot();
    }
}
