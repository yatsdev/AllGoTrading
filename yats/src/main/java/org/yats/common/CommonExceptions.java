package org.yats.common;

public class CommonExceptions {

    public static class ContainerEmptyException extends RuntimeException {
        public ContainerEmptyException(String msg) {
            super(msg);
        }
    }

    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String msg) {
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

    public static class NetworkException extends RuntimeException {
        public NetworkException(String s) { super(s);
        }
    }

} // class
