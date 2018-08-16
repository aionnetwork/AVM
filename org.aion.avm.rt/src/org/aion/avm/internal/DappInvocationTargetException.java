package org.aion.avm.internal;

public class DappInvocationTargetException extends AvmException {
    private static final long serialVersionUID = 1L;

    public DappInvocationTargetException() {
        super();
    }

    public DappInvocationTargetException(String message) {
        super(message);
    }

    public DappInvocationTargetException(String message, Throwable cause) {
        super(message, cause);
    }

    public DappInvocationTargetException(Throwable cause) {
        super(cause);
    }
}
