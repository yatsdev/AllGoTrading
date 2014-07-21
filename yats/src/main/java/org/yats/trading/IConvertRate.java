package org.yats.trading;

public interface IConvertRate {

    public Position convert(Position position, String targetProductId);

}
