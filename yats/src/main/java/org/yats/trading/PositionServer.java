package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class PositionServer implements IConsumeReceipt, IProvidePosition, IProvideProfit{


    final Logger log = LoggerFactory.getLogger(PositionServer.class);

    public String getPositionSnapshotCSV() {
        return positionSnapshot.toStringCSV();
    }

    @Override
    public void onReceipt(Receipt receipt) {
        if (!receipt.isTrade()) return;
        numberOfReceipts++;
        AccountPosition positionChange = receipt.toAccountPosition();
        positionSnapshot.add(positionChange);
        log.info("new position snapshot: "+positionSnapshot.toStringCSV());
        positionStorage.store(positionSnapshot);
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


    public PositionSnapshot getPositionSnapshot() {
        return positionSnapshot;
    }

    public boolean isEmpty() {
        return positionSnapshot.size()==0;
    }

    public void setPositionSnapshot(PositionSnapshot positionSnapshot) {
        this.positionSnapshot = positionSnapshot;
    }

    public void addPositionSnapshot(PositionSnapshot newPositionSnapshot) {
        positionSnapshot.add(newPositionSnapshot);
    }

    public void clearPositions() {
        positionSnapshot = new PositionSnapshot();
    }


    public PositionServer() {
        numberOfReceipts = 0;
        positionSnapshot = new PositionSnapshot();
        positionStorage = new IStorePositionSnapshots() {
            @Override
            public void store(PositionSnapshot positionSnapshot) {
            }
            @Override
            public PositionSnapshot readLast() {
                throw new NotImplementedException();
            }
        };

    }

    private int numberOfReceipts;
    private PositionSnapshot positionSnapshot;
    private IStorePositionSnapshots positionStorage;

    public void setPositionStorage(IStorePositionSnapshots positionStorage) {
        this.positionStorage = positionStorage;
    }

    public void initFromLastStoredPositionSnapshot() {
        positionSnapshot = positionStorage.readLast();
    }
}
