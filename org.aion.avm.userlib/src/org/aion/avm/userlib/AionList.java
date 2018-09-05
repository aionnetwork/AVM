package org.aion.avm.userlib;


import java.util.*;

/**
 * The first rough cut of the List-like abstraction we are providing to our user-space apps.
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
public class AionList<E> implements List<E> {
    private static final int DEFAULT_CAPACITY = 5;

    private Object[] storage;

    private int size;

    public AionList() {
        this.storage = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public void trimToSize() {
        if (size < storage.length) {
            Object[] tmp = this.storage;
            this.storage = new Object[size];
            System.arraycopy(tmp, 0, this.storage, 0, size);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean contains(Object toFind) {
        return indexOf(toFind) >= 0;
    }

    @Override
    public boolean isEmpty() {
        return 0 == size;
    }

    @Override
    public Object[] toArray() {
        Object[] ret = new Object[size];
        System.arraycopy(this.storage, 0, ret, 0, size);
        return ret;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        E result = null;
        if (index < this.size) {
            result = (E) this.storage[index];
        }
        return result;
    }

    @Override
    public E set(int index, E element) {
        E oldData = null;
        if (index < this.size) {
            oldData = (E) this.storage[index];
            this.storage[index] = element;
        }
        return oldData;
    }

    @Override
    public boolean add(E newElement) {

        if (this.size == this.storage.length)
            this.storage = grow();
        this.storage[size] = newElement;
        size = size + 1;

        return true;
    }

    @Override
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        if (this.size == this.storage.length)
            this.storage = grow();
        System.arraycopy(this.storage, index, this.storage, index + 1,size - index);
        this.storage[index] = element;
        size = size + 1;
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException();
    }

    private void rangeCheckForRemove(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException();
    }

    private Object[] grow(){
        Object[] newStorage = new Object[2 * this.storage.length];
        System.arraycopy(this.storage, 0, newStorage, 0, this.storage.length);
        return newStorage;
    }


    @Override
    public E remove(int index) {

        rangeCheckForRemove(index);

        E oldValue = (E) this.storage[index];
        if ((size - 1) > index)
            System.arraycopy(this.storage, index + 1, this.storage, index, size - 1 - index);
        this.storage[size] = null;
        size = size - 1;

        return oldValue;
    }

    @Override
    public boolean remove(Object toRemove) {
        boolean ret = false;
        int index = indexOf(toRemove);
        if (index >= 0){
            this.remove(index);
            ret = true;
        }
        return ret;
    }

    @Override
    public void clear() {
        this.storage = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = false;
        for (E obj: c){
            this.add(obj);
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean ret = false;
        for (E obj: c){
            this.add(index, obj);
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj: c){
            if (!this.contains(obj)) return false;
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        Iterator<E> it = this.iterator();
        while(it.hasNext()){
            if (!c.contains(it.next())){
                it.remove();
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = false;
        Iterator<E> it = this.iterator();
        while(it.hasNext()){
            if (c.contains(it.next())){
                it.remove();
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (storage[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(storage[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (storage[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(storage[i]))
                    return i;
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        return new AionListIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if ((index < 0) || (index > this.size)) {
            throw new IndexOutOfBoundsException();
        }
        return new AionListIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO:  Add sublist handling if we want to continue using this implementation (not included for alpha release).
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        return new AionListIterator(0);
    }


    /**
     * Note that this is a very simple implementation so it skips all optional operations and doesn't detect concurrent modifications.
     * (public only to reference it from tests when building the DApp jar)
     */
    public class AionListIterator implements ListIterator<E> {
        private int nextIndex;
        public AionListIterator(int nextIndex) {
            this.nextIndex = nextIndex;
        }
        @Override
        public boolean hasNext() {
            return this.nextIndex < AionList.this.size;
        }
        @Override
        public E next() {
            E elt = null;
            if (this.nextIndex < AionList.this.size) {
                elt = (E) AionList.this.storage[this.nextIndex];
                this.nextIndex += 1;
            } else {
                // TODO:  NoSuchElementException is in java.util, not currently included in shadow.
                elt = null;
            }
            return elt;
        }
        @Override
        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }
        @Override
        public E previous() {
            E elt = null;
            if (this.nextIndex > 0) {
                this.nextIndex -= 1;
                elt = (E) AionList.this.storage[this.nextIndex];
            } else {
                // TODO:  NoSuchElementException is in java.util, not currently included in shadow.
                elt = null;
            }
            return elt;
        }
        @Override
        public int nextIndex() {
            return this.nextIndex;
        }
        @Override
        public int previousIndex() {
            return this.nextIndex - 1;
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }
}
