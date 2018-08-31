package org.aion.avm.userlib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BSet<E> implements Set<E> {

    private BMap<E,Object> map;

    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing {@code HashMap} instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public BSet() {
        map = new BMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    @Override
    public boolean remove(Object o) {
        return (null != map.remove(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c){
            if (!this.contains(obj)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (Object obj : c){
            if (!this.add((E) obj)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }

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

    @Override
    public Iterator<E> iterator() {
        return new BSetIterator(this.size());
    }

    public final class BSetIterator implements Iterator<E> {
        BMap.BLeafNode curLeaf;
        BMap.BEntry curEntry;
        int curSlot;

        public BSetIterator(int size){
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
            }

            // Advance cursor
            if (null != curEntry.next){
                curEntry = curEntry.next;
            }else if (curSlot + 1 < curLeaf.nodeSize){
                curSlot++;
                curEntry = curLeaf.entries[curSlot];
            }else if (null != curLeaf.next){
                curLeaf = (BMap.BLeafNode) curLeaf.next;
                curSlot = 0;
                curEntry = curLeaf.entries[curSlot];
            }else{
                curEntry = null;
            }

            return elt;
        }
    }
}
