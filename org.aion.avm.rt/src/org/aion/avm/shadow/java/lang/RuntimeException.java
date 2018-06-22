package org.aion.avm.shadow.java.lang;


/**
 * Our shadow implementation of java.lang.RuntimeException.
 * 
 * This only exists as an intermediary since we needed to implement a few specific subclasses.
 */
public class RuntimeException extends Exception {
    public RuntimeException() {
        super();
    }

    public RuntimeException(String message) {
        super(message);
    }

    public RuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeException(Throwable cause) {
        super(cause);
    }
}
