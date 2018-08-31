package org.aion.avm.userlib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * The first rough cut of the Set-like abstraction we are providing to our user-space apps.
 * Note:  This implementation depends on elements having a sensible hashCode() implementation.
 * If we proceed with this direction, we will need to improve this implementation/interface significantly, as it only exists
 * to cover very basic uses, at the moment.
 * 
 * Note about userlib package:
 * This is an approach to implementing something analogous to the Java Collections purely in the "user-space" of the contract.
 * That is, these classes are transformed and accessible to contract code, without any special support from our VM.
 * We may expand, change, or remove this idea in favour of something else, as we proceed.  This solution isn't set in stone.
 * 
 * TODO:  This is a VERY basic implementation which must be replace if we expect to proceed this way.
 * We might also want to make the class into a constructor argument, so we can add more aggressive type safety to the internals.
 */

@Deprecated
public class AionPlainSet<E> implements Set<E> {
    private static final int kStartSize = 1;

    // To avoid faulting in all the objects in storage, we cache their hashes to see if it is even worth checking.
    private int[] hashes;
    private Object[] storage;

    public AionPlainSet() {
        this.hashes = new int[kStartSize];
        this.storage = new Object[kStartSize];
    }

    public boolean add(E newElement) {
        // This implementation is very simple so we just walk the list, seeing if this is already here.
        int newHash = newElement.hashCode();
        int insertIndex = this.storage.length;
        for (int i = 0; i < this.storage.length; ++i) {
            Object elt = this.storage[i];
            if (null == elt) {
                // The data is always packed into the beginning so the first hole is the end.
                insertIndex = i;
                break;
            } else {
                // Check the hash cache, first (equals() will fault in the object).
                if ((newHash == this.hashes[i]) && newElement.equals(elt)) {
                    insertIndex = -1;
                    break;
                } else {
                    // Keep searching.
                }
            }
        }
        
        if (insertIndex >= 0) {
            if (insertIndex >= this.storage.length) {
                // Grow.
                int[] newHashes = new int[this.hashes.length * 2];
                Object[] newStorage = new Object[this.storage.length * 2];
                System.arraycopy(this.hashes, 0, newHashes, 0, this.hashes.length);
                System.arraycopy(this.storage, 0, newStorage, 0, this.storage.length);
                this.hashes = newHashes;
                this.storage = newStorage;
            }
            // Now, insert.
            this.hashes[insertIndex] = newHash;
            this.storage[insertIndex] = newElement;
            return true;
        }
        return false;
    }

    public boolean contains(Object check) {
        int hash = check.hashCode();
        boolean doesContain = false;
        for (int i = 0; !doesContain && (i < this.storage.length) && (null != this.storage[i]); ++i) {
            // Check the hash cache, first (equals() will fault in the object).
            doesContain = (hash == this.hashes[i]) && check.equals(this.storage[i]);
        }
        return doesContain;
    }

    public boolean remove(Object toRemove) {
        int hash = toRemove.hashCode();
        int foundIndex = -1;
        for (int i = 0; (-1 == foundIndex) && (i < this.storage.length) && (null != this.storage[i]); ++i) {
            // Check the hash cache, first (equals() will fault in the object).
            if ((hash == this.hashes[i]) && toRemove.equals(this.storage[i])) {
                foundIndex = i;
            }
        }
        
        if (-1 != foundIndex) {
            // Shift everything to beginning, over-writing the removed element.
            // (we could arraycopy this if we wanted to keep track of the end index).
            for (int i = foundIndex; i < this.storage.length; ++i) {
                int hashMove = ((i + 1) < this.hashes.length)
                        ? this.hashes[i+1]
                        : 0;
                Object toRead = ((i + 1) < this.storage.length)
                        ? this.storage[i+1]
                        : null;
                this.hashes[i] = hashMove;
                this.storage[i] = toRead;
            }
        }
        return (-1 != foundIndex);
    }

    @Override
    public boolean isEmpty() {
        return 0 == size();
    }

    public int size() {
        int size = 0;
        for (Object elt : this.storage) {
            if (null != elt) {
                size += 1;
            } else {
                break;
            }
        }
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] ret = new Object[size()];
        int i = 0;

        for (Object elt : this.storage) {
            if (null != elt) {
                ret[i++] = elt;
            }
        }
        return ret;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // NOTE:  This is explicitly excluded from the shadow version of the Set interface so we don't need to implement it.
        return null;
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
        for (E obj : c){
            if (!this.add(obj)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        for (E obj : this){
            if (!c.contains(obj)){
                this.remove(obj);
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = false;
        for (E obj : this){
            if (c.contains(obj)){
                this.remove(obj);
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public void clear() {
        this.hashes = new int[kStartSize];
        this.storage = new Object[kStartSize];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Set)) return false;

        Set that = (Set)o;

        if (this.size() != that.size()) return false;

        for (Object obj : this){
            if (!that.contains(obj)) return false;
        }

        for (Object obj : that){
            if (!this.contains(obj)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for (E obj : this){
            if (null != obj){
                ret += obj.hashCode();
            }
        }
        return ret;
    }

    @Override
    public Iterator<E> iterator() {
        return new AionSetIterator(this.size());
    }

    public final class AionSetIterator implements Iterator<E> {
        int nextIndex;
        final int endIndex; // (exclusive)
        public AionSetIterator(int size){
            this.nextIndex = 0;
            this.endIndex = size;
        }
        @Override
        public boolean hasNext() {
            return (this.nextIndex < this.endIndex);
        }
        @Override
        public E next() {
            E elt = null;
            if (this.nextIndex < this.endIndex) {
                // The storage of the parent array is dense so we can always just grab the next.
                // If the array was modified underneath us, we may not return all the objects or may return null.
                elt = (E) AionPlainSet.this.storage[this.nextIndex];
                this.nextIndex += 1;
            } else {
                // TODO:  NoSuchElementException is in java.util, not currently included in shadow.
                elt = null;
            }
            return elt;
        }
    }
}
