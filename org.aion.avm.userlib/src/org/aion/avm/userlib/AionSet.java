package org.aion.avm.userlib;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class AionSet<E> implements Set<E> {

    private AionMap<E,Object> map;

    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing {@code AionMap} instance has
     * default order(4).
     */
    public AionSet() {
        map = new AionMap<>();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * More formally, returns {@code true} if and only if this set
     * contains an element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }


    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("toArray");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("toArray");
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element {@code e} to this set if
     * this set contains no element {@code e2} such that
     * {@code Objects.equals(e, e2)}.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns {@code false}.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified
     * element
     */
    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element {@code e} such that
     * {@code Objects.equals(o, e)},
     * if this set contains such an element.  Returns {@code true} if
     * this set contained the element (or equivalently, if this set
     * changed as a result of the call).  (This set will not contain the
     * element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if the set contained the specified element
     */
    @Override
    public boolean remove(Object o) {
        return (null != map.remove(o));
    }

    /**
     * This implementation iterates over the specified collection,
     * checking each element returned by the iterator in turn to see
     * if it's contained in this collection.  If all elements are so
     * contained {@code true} is returned, otherwise {@code false}.
     *
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c){
            if (!this.contains(obj)){
                return false;
            }
        }
        return true;
    }


    /**
     * This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.
     *
     * @see #add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }

    /**
     * Removes all of the elements from this set.
     * The set will be empty after this call returns.
     */
    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set
     */
    @Override
    public Iterator<E> iterator() {
        return new AionSetIterator();
    }

    public final class AionSetIterator implements Iterator<E> {
        AionMap.BLeafNode curLeaf;
        AionMap.AionMapEntry curEntry;
        int curSlot;

        public AionSetIterator(){
            curLeaf = map.getLeftMostLeaf();
            curSlot = 0;
            curEntry = curLeaf.entries[curSlot];
        }

        @Override
        public boolean hasNext() {
            return (null != curEntry);
        }

        @Override
        public E next() {
            E elt = null;

            if (null != curEntry){
                elt = (E) curEntry.getKey();

                // Advance cursor
                if (null != curEntry.next){
                    curEntry = curEntry.next;
                }else if (curSlot + 1 < curLeaf.nodeSize){
                    curSlot++;
                    curEntry = curLeaf.entries[curSlot];
                }else if (null != curLeaf.next){
                    curLeaf = (AionMap.BLeafNode) curLeaf.next;
                    curSlot = 0;
                    curEntry = curLeaf.entries[curSlot];
                }else{
                    curEntry = null;
                }
            } else {
                throw new NoSuchElementException();
            }

            return elt;
        }
    }
}
