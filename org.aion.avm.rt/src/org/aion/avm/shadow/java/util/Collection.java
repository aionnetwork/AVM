package org.aion.avm.shadow.java.util;

import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Iterable;

public interface Collection<E> extends Iterable<E>{

    // Query Operations

    int avm_size();

    boolean avm_isEmpty();

    boolean avm_contains(Object o);

    ObjectArray avm_toArray();

    boolean avm_add(E e);

    boolean avm_remove(Object o);

    boolean avm_containsAll(Collection<?> c);

    boolean avm_addAll(Collection<? extends E> c);

    boolean avm_removeAll(Collection<?> c);

    boolean avm_retainAll(Collection<?> c);

    void avm_clear();

    boolean avm_equals(IObject o);

    int avm_hashCode();

    //Default


    //Exclude

    //<T> T[] toArray(T[] a);
}
