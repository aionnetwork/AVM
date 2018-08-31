package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;

import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * Our shadow implementation of java.lang.Error.
 * 
 * This only exists as an intermediary since we needed to implement AssertionError.
 */
public class Error extends Throwable {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public Error() {
        super();
    }

    public Error(String message) {
        super(message);
    }

    public Error(String message, Throwable cause) {
        super(message, cause);
    }

    public Error(Throwable cause) {
        super(cause);
    }

    // Deserializer support.
    public Error(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }
}
