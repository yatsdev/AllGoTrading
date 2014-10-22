package org.yats.messagebus;

import com.google.gson.Gson;

public class Serializer<T> {

    public String convertToString(T data) {
        Gson serializer = new Gson();
        String msgString = serializer.toJson(data);
        return msgString;
    }

    public Serializer() {
    }

} // class
