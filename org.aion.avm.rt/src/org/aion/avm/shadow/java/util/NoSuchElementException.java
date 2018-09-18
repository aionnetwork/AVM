package org.aion.avm.shadow.java.util;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.RuntimeException;
import org.aion.avm.shadow.java.lang.String;


/**
 * Our shadow implementation of java.util.NoSuchElementException.
 * 
 * Implemented manually since it only provides a subset of the usual exception constructors.
 */
public class NoSuchElementException extends RuntimeException {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public NoSuchElementException() {
        super();
    }

    public NoSuchElementException(String message) {
        super(message);
    }

    // Deserializer support.
    public NoSuchElementException(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }
}
