package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;

import org.aion.avm.internal.IPersistenceToken;

/**
 * Our shadow implementation of java.lang.EnumConstantNotPresentException.
 */
@SuppressWarnings("rawtypes") /* rawtypes are part of the public api */
public class EnumConstantNotPresentException extends RuntimeException {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    private Class<? extends Enum> enumType;
    private String constantName;

    public EnumConstantNotPresentException(Class<? extends Enum> enumType, String constantName) {
        super(new String(enumType.avm_getName() + "." + constantName));
        this.enumType = enumType;
        this.constantName  = constantName;
    }

    public EnumConstantNotPresentException(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public String avm_constantName() {
        lazyLoad();
        return this.constantName;
    }

    public Class<? extends Enum> avm_enumType() {
        lazyLoad();
        return this.enumType;
    }
}
