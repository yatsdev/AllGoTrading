package org.yats.trading;

/**
 * Created by macbook52 on 14/10/14.
 */
public interface IStorePrice {
    public void store(PriceData p);

    public PriceData readLast();
}
