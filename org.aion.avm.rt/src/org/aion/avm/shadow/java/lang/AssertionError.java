package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IPersistenceToken;

/**
 * Our shadow implementation of java.lang.AssertionError.
 * 
 * This requires manual implementation since it has many constructors.
 */
public class AssertionError extends Error {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public AssertionError() {
    }

    private AssertionError(String detailMessage) {
        super(detailMessage);
    }

    public AssertionError(IObject detailMessage) {
        this(String.avm_valueOf((Object)detailMessage), (detailMessage instanceof Throwable) ? (Throwable) detailMessage : null);
    }

    public AssertionError(boolean detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(char detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(int detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(long detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(float detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(double detailMessage) {
        this(String.avm_valueOf(detailMessage));
    }

    public AssertionError(String message, Throwable cause) {
        super(message, cause);
    }

    // Deserializer support.
    public AssertionError(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }
}
