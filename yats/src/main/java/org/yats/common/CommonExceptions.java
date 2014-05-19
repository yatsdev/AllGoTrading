package org.yats.common;

public class CommonExceptions {

    public static void throwKeyNotFoundInConfigFile(String msg) {
        throw new KeyNotFoundInConfigFile(msg);
    }

    public static void throwFileReadException(String message) {
        throw new FileReadException(message);
    }

    public static void throwFileWriteException(String message) {
        throw new FileWriteException(message);
    }


    public static class KeyNotFoundInConfigFile extends RuntimeException {
        public KeyNotFoundInConfigFile(String msg) {
            super(msg);
        }
    }

    public static class FileReadException extends RuntimeException {
        public FileReadException(String msg) {
            super(msg);
        }
    }

    public static class FileWriteException extends RuntimeException {
        public FileWriteException(String msg) {
            super(msg);
        }
    }

} // class
