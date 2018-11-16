package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;

import org.aion.avm.internal.IPersistenceToken;

/**
 * Our shadow implementation of java.lang.TypeNotPresentException.
 */
public class TypeNotPresentException extends RuntimeException {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    private String typeName;

    public TypeNotPresentException(String typeName, Throwable cause) {
        super(new String("Type " + typeName + " not present"), cause);
        this.typeName = typeName;
    }

    public TypeNotPresentException(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public String typeName() {
        lazyLoad();
        return typeName;
    }
}
