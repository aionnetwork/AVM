package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IObject;

public interface Comparable<T extends IObject> extends IObject {

    public int avm_compareTo(T o);
}
