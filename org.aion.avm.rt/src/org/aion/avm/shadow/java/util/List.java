package org.aion.avm.shadow.java.util;

import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.internal.IObject;

public interface List<E> extends Collection<E> {

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

    // Positional Access Operations

    E avm_get(int index);

    E avm_set(int index, E element);

    void avm_add(int index, E element);

    E avm_remove(int index);

    int avm_indexOf(Object o);

    int avm_lastIndexOf(Object o);

    ListIterator<E> avm_listIterator();

    ListIterator<E> avm_listIterator(int index);

    // View

    List<E> avm_subList(int fromIndex, int toIndex);

}
