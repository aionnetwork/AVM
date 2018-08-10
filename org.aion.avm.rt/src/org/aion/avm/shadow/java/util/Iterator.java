package org.aion.avm.shadow.java.util;

public interface Iterator<E> {
    boolean avm_hasNext();

    E avm_next();

    default void avm_remove() {
        throw new UnsupportedOperationException("remove");
    }
}
