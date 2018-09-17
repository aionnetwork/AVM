package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;

/**
 * Our shadow implementation of java.nio.BufferOverflowException.
 */
public class BufferOverflowException extends RuntimeException {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private static final long serialVersionUID = -5484897634419144535L;

    /**
     * Constructs an instance of this class.
     */
    public BufferOverflowException() { }

}
