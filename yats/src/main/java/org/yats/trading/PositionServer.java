package org.yats.trading;

import org.yats.common.Decimal;

import java.util.ArrayList;
import java.util.List;

public class PositionServer implements IConsumeReceipt, IProvidePosition {

    public PositionServer() {
        numberOfReceipts = 0;
        positionSnapshot = new PositionSnapshot();
    }

    @Override
    public void onReceipt(Receipt receipt) {
        if (receipt.isRejection()) return;
        if (!receipt.isTrade()) return;
        numberOfReceipts++;
        AccountPosition positionChange
                = new AccountPosition(receipt.getProductId(), receipt.getPositionChange(), receipt.getInternalAccount());
        positionSnapshot.add(positionChange);
    }

    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
        return new AccountPosition("pid", Decimal.ZERO, "account");
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
