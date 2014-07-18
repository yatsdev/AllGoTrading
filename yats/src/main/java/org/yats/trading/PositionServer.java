package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

public class PositionServer implements IConsumeReceipt, IProvidePosition {


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
    public Position getValueForAccountProduct(RateConverter converter, PositionRequest request, String targetProductId) {
        Position p = positionSnapshot.getAccountPosition(request);
        Position targetPosition = converter.convert(p, targetProductId);
        return targetPosition;
    }

    @Override
    public Position getValueForAccount(RateConverter converter, String accountId, String targetProductId) {
        return positionSnapshot.getValueForAccount(converter, accountId, targetProductId);
    }

    @Override
    public Position getValueForAllPositions(RateConverter converter, String targetProductId) {
        return positionSnapshot.getValueForAllPositions(converter, targetProductId);
    }

    @Override
    public IProvidePosition getAllPositionsForOneAccount(String accountId) {
        return positionSnapshot.getAllPositionsForOneAccount(accountId);
    }

    @Override
    public Collection<AccountPosition> getAllPositions() {
        return positionSnapshot.getAllPositions();
    }


    public AccountPosition getAccountPosition(PositionRequest positionRequest) {
        return positionSnapshot.getAccountPosition(positionRequest);
    }

    public Position getPositionForAllAccounts(String productId) {
        return positionSnapshot.getPositionForAllAccounts(productId);
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

    public void setRateConverter(RateConverter rateConverter) {
        this.rateConverter = rateConverter;
    }

    public PositionServer() {
        numberOfReceipts = 0;
        rateConverter = new RateConverter(new ProductList());
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
    private RateConverter rateConverter;

    public void setPositionStorage(IStorePositionSnapshots positionStorage) {
        this.positionStorage = positionStorage;
    }

    public void initFromLastStoredPositionSnapshot() {
        positionSnapshot = positionStorage.readLast();
        log.info("PositionServer starting position: "+positionSnapshot.toStringCSV());
    }
}
