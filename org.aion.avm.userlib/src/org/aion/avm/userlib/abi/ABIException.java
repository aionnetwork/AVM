package org.aion.avm.userlib.abi;

public class ABIException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ABIException(String message) {
        super(message);
    }
}
