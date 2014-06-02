package org.yats.trading;

public interface IStorePositionSnapshots {
    public void store(PositionSnapshot positionSnapshot);
    public PositionSnapshot readLast();
}
