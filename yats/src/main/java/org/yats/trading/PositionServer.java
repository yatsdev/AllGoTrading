package org.yats.trading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

public class PositionServer implements IConsumeReceipt, IProvidePosition {


    final Logger log = LoggerFactory.getLogger(PositionServer.class);

    @Override
    public void onReceipt(Receipt receipt) {
        if (!receipt.isTrade()) return;
        if(!productList.containsProductWith(receipt.getProductId()))
            throw new TradingExceptions.UnknownIdException("Unknown product in receipt "+receipt);
        numberOfReceipts++;
        AccountPosition positionChangeOfBase = receipt.getPositionChangeOfBaseAsAccountPosition();
        positionSnapshot.add(positionChangeOfBase);

        AccountPosition counterPosition = receipt.getPositionChangeOfCounterAsAccountPosition(productList);
        positionSnapshot.add(counterPosition);
        log.info("new position snapshot: "+positionSnapshot.toStringCSV());
        positionStorage.store(positionSnapshot);
    }

    @Override
    public Position getValueForAccountProduct(String targetProductId, PositionRequest request) {
        return positionSnapshot.getValueForAccountProduct(targetProductId, request);
    }

    @Override
    public Position getValueForAccount(String targetProductId, String accountId) {
        return positionSnapshot.getValueForAccount(targetProductId, accountId);
    }

    @Override
    public Position getValueForAllPositions(String targetProductId) {
        return positionSnapshot.getValueForAllPositions(targetProductId);
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

    public void setPositionSnapshot(PositionSnapshot _positionSnapshot)
    {
        positionSnapshot = _positionSnapshot;
        positionSnapshot.setRateConverter(rateConverter);
    }

    public void addPositionSnapshot(PositionSnapshot newPositionSnapshot) {
        positionSnapshot.add(newPositionSnapshot);
    }

    public void setProductList(IProvideProduct productList) {
        this.productList = productList;
    }

    public void setRateConverter(IConvertRate _rateConverter)
    {
        rateConverter = _rateConverter;
        positionSnapshot.setRateConverter(rateConverter);
    }

    public void setPositionStorage(IStorePositionSnapshots positionStorage) {
        this.positionStorage = positionStorage;
    }

    public void initFromLastStoredPositionSnapshot() {
        positionSnapshot = positionStorage.readLast();
        log.info("PositionServer starting position: "+positionSnapshot.toStringCSV());
    }

    public PositionServer() {
        rateConverter = new RateConverter(new ProductList());
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
        productList = new ProductList();

    }

    private IProvideProduct productList;
    private int numberOfReceipts;
    private PositionSnapshot positionSnapshot;
    private IStorePositionSnapshots positionStorage;
    private IConvertRate rateConverter;

}
