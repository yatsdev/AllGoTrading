package org.yats.messagebus;

import flexjson.JSONSerializer;

public class Serializer<T> {

    public String convertToString(T data) {
        JSONSerializer serializer = new JSONSerializer();
        String msgString = serializer.serialize(data);
        return msgString;
    }

    public Serializer() {
    }

} // class
