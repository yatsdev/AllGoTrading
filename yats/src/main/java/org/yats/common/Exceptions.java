package org.yats.common;

public class Exceptions {

    public static void throwKeyNotFoundInConfigFile(String msg) {
        throw new KeyNotFoundInConfigFile(msg);
    }

    public static class KeyNotFoundInConfigFile extends RuntimeException {
        public KeyNotFoundInConfigFile(String msg) {
            super(msg);
        }
    }

} // class
