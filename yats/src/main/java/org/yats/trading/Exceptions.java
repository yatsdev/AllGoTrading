package org.yats.trading;

public class Exceptions {


    public static void throwItemNotFoundException(String msg) {
        throw new ItemNotFoundException(msg);
    }
    public static void throwFieldIsNullException(String msg) {
        throw new FieldIsNullException(msg);
    }

    public static void throwFileReadException(String message) {
        throw new FileReadException(message);
    }

    public static void throwFileWriteException(String message) {
        throw new FileWriteException(message);
    }

    public static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String msg) {
            super(msg);
        }
    }
    public static class FieldIsNullException extends RuntimeException {
        public FieldIsNullException(String msg) {
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
