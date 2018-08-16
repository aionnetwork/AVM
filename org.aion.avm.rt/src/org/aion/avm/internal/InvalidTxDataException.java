package org.aion.avm.internal;

/**
 * Exception that indicates the transaction data is not valid; thus it cannot be properly decoded into a method caller or arguments.
 */
public class InvalidTxDataException extends AvmException {
    private static final long serialVersionUID = 1L;

    public InvalidTxDataException() {
        super();
    }

    public InvalidTxDataException(String message) {
        super(message);
    }

    public InvalidTxDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTxDataException(Throwable cause) {
        super(cause);
    }
}
