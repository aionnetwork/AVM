package org.aion.avm.internal;

/**
 * Error that indicates the transaction data cannot be properly decoded, or cannot be converted to the method arguments.
 */
public class ABICodecException extends AvmException {
    private static final long serialVersionUID = 1L;

    public ABICodecException(String message) {
        super(message);
    }
}
