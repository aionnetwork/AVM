package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.util.Iterator;

public interface Iterable<T> extends IObject {
    Iterator<T> avm_iterator();

    //Default
}
