package org.aion.avm.shadow.java.util;

import org.aion.avm.internal.IObject;

public interface Iterator<E> extends IObject {
    boolean avm_hasNext();

    IObject avm_next();

    default void avm_remove() {
        throw new UnsupportedOperationException("remove");
    }
}
