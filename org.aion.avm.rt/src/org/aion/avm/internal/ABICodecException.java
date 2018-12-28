package org.aion.avm.internal;


/**
 * Error that indicates the transaction data cannot be properly decoded, or cannot be converted to the method arguments.
 * Note that this type is an AvmException, meaning that it is considered fatal to the user DApp where it occurred.
 */
public class ABICodecException extends AvmException {
    private static final long serialVersionUID = 1L;

    public ABICodecException(String message) {
        super(message);
    }

    public ABICodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
