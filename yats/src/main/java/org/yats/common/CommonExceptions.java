package org.yats.common;

public class CommonExceptions {

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

    public static class FieldNotFoundException extends RuntimeException {
        public FieldNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class CouldNotInstantiateClassException extends RuntimeException {
        public CouldNotInstantiateClassException(String msg) {
            super(msg);
        }
    }

    public static class DummyException extends RuntimeException {
        public DummyException(String s) { super(s);
        }
    }
} // class
