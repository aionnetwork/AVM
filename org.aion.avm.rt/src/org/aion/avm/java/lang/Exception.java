package org.aion.avm.java.lang;


/**
 * Our shadow implementation of java.lang.Exception.
 * 
 * This only exists as an intermediary since we needed to implement a few specific subclasses.
 */
public class Exception extends Throwable {
    public Exception() {
        super();
    }

    public Exception(String message) {
        super(message);
    }

    public Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Exception(Throwable cause) {
        super(cause);
    }
}
