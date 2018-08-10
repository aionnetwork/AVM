package org.aion.avm.shadow.java.lang;

import org.aion.avm.shadow.java.util.Iterator;

public interface Iterable<T> {
    Iterator<T> avm_iterator();

    //Default
}
