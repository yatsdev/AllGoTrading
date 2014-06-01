package org.yats.common;

public interface IProvideProperties {

    boolean exists(String _key);
    String get(String _key);
    String get(String _key, String _defaultValue);
    boolean getAsBoolean(String _key, boolean _defaultValue);

}
