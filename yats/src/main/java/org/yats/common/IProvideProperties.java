package org.yats.common;

import java.util.Set;

public interface IProvideProperties {

    int size();
    boolean exists(String _key);
    String get(String _key);
    String get(String _key, String _defaultValue);
    boolean getAsBoolean(String _key);
    boolean getAsBoolean(String _key, boolean _defaultValue);
    Decimal getAsDecimal(String _key);
    String[] getCSVAsArray(String _key);
    Set<String> getKeySet();

    void set(String key, boolean value);
    void set(String key, Decimal value);
    void set(String key, String value);

}
