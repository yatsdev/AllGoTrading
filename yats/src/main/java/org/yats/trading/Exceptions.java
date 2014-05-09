package org.yats.trading;

public class Exceptions {


    public static void throwItemNotFoundException(String msg) {
        throw new ItemNotFoundException(msg);
    }

    public static void throwFileReadException(String message) {
        throw new FileReadException(message);
    }

    private static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String msg) {
            super(msg);
        }
    }

    private static class FileReadException extends RuntimeException {
        public FileReadException(String msg) {
            super(msg);
        }
    }
} // class
