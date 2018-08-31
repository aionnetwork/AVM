package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;

import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * Our shadow implementation of java.lang.RuntimeException.
 * 
 * This only exists as an intermediary since we needed to implement a few specific subclasses.
 */
public class RuntimeException extends Exception {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

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

    // Deserializer support.
    public RuntimeException(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }
}
