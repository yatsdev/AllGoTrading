package org.yats.messagebus;

import com.google.gson.Gson;

public class Deserializer<T>
{

    public T convertFromString(String msg) {
        Gson deserializer = new Gson();
        T data = deserializer.fromJson(msg, tClass);
        return data;
    }

    public Deserializer(Class<T> _tClass) {
        tClass = _tClass;
    }

    Class<T> tClass;
} // class
