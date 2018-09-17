package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;

/**
 * Our shadow implementation of java.nio.BufferUnderflowException.
 */
public class BufferUnderflowException extends RuntimeException {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private static final long serialVersionUID = -1713314658691622206L;

    /**
     * Constructs an instance of this class.
     */
    public BufferUnderflowException() { }

}
