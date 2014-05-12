package org.yats.messagebus;

import flexjson.JSONDeserializer;

public class Deserializer<T>
{

    public T convertFromString(String msg) {
        T data = new JSONDeserializer<T>().deserialize(msg, tClass);
        return data;
    }

    public Deserializer(Class<T> _tClass) {
        tClass = _tClass;
    }

    Class<T> tClass;
} // class
