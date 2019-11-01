package org.aion.avm.userlib;

import java.util.*;


/**
 * A simple List implementation.
 * 
 * <p>This implementation is backed by a single Object[], starts at 5 elements and doubles when full.
 * 
 * @param <E> The type of elements within the set.
 */
public class AionList<E> implements List<E> {
    private static final int DEFAULT_CAPACITY = 5;

    private Object[] storage;

    private int size;

    private int modCount;

    public AionList() {
        this.storage = new Object[DEFAULT_CAPACITY];
        this.size = 0;
        this.modCount = 0;
    }

    public void trimToSize() {
        modCount++;
        if (size < storage.length) {
            Object[] tmp = this.storage;
            this.storage = new Object[size];
            System.arraycopy(tmp, 0, this.storage, 0, size);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     *
     * @param toFind element to find in the list
     * @return {@code true} if this list contains the specified element, {@code false} otherwise
     */
    @Override
    public boolean contains(Object toFind) {
        return indexOf(toFind) >= 0;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return 0 == size;
    }

    /**
     * Returns an array containing all of the elements in this list with the same order.
     *
     * @return an array containing all of the elements in this list
     */
     @Override
    public Object[] toArray() {
        Object[] ret = new Object[size];
        System.arraycopy(this.storage, 0, ret, 0, size);
        return ret;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to get
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        checkIndex(index);
        return (E) this.storage[index];
    }

    /**
     * Replaces the element at the specified index in this list with the new element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified index
     * @return the element previously at the specified index
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        checkIndex(index);
        E oldData = (E) this.storage[index];
        this.storage[index] = element;
        return oldData;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param newElement element to be appended to this list
     * @return {@code true}
     */
    @Override
    public boolean add(E newElement) {

        if (this.size == this.storage.length)
            this.storage = grow();
        this.storage[size] = newElement;
        size = size + 1;
        modCount++;

        return true;
    }

    /**
     * Inserts the specified element at the specified index and shifts the subsequent list
     * elements to the right.
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @Override
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        insertAtIndex(index, element);
        modCount++;
    }

    private void insertAtIndex(int index, E element) {
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

    private void checkIndex(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException();
    }

    private Object[] grow(){
        Object[] newStorage = new Object[2 * this.storage.length];
        System.arraycopy(this.storage, 0, newStorage, 0, this.storage.length);
        return newStorage;
    }

    /**
     * Removes the element at the specified index in this list and shifts any subsequent elements to the left..
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        checkIndex(index);

        E oldValue = (E) this.storage[index];
        removeElementAtIndex(index);
        modCount++;

        return oldValue;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it will not be modified.
     *
     * @param toRemove element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element and it was removed
     */
    @Override
    public boolean remove(Object toRemove) {
        boolean ret = false;
        int index = indexOf(toRemove);
        if (index >= 0){
            removeElementAtIndex(index);
            modCount++;
            ret = true;
        }
        return ret;
    }

    private void removeElementAtIndex(int index){
        size = size - 1;
        if (size > index) {
            System.arraycopy(this.storage, index + 1, this.storage, index, size - index);
        }
        this.storage[size] = null;
    }

    /**
     * Removes all of the elements from this list.
     */
    @Override
    public void clear() {
        this.storage = new Object[DEFAULT_CAPACITY];
        this.size = 0;
        modCount++;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list.
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        modCount++;
        if (c.size() == 0) {
            return false;
        } else {
            boolean ret = false;
            for (E obj : c) {
                insertAtIndex(this.size, obj);
                ret = true;
            }
            return ret;
        }
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified index. Subsequent elements in the
     * list are shifted to the right.
     *
     * @param index index at which to insert the first element of the specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if index is out of range
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        modCount++;
        if (c.size() == 0) {
            return false;
        } else {
            boolean ret = false;
            for (E obj : c) {
                insertAtIndex(index, obj);
                index++;
                ret = true;
            }
            return ret;
        }
    }

    /**
     * Returns {@code true} If all elements in specified collection are contained in this list
     *
     * @return {@code true} If all elements in specified collection are contained in this list, {@code false} otherwise
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj: c){
            if (!this.contains(obj)) return false;
        }
        return true;
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     */
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

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     */
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

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
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

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
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

    /**
     * Returns a list iterator over the elements in this list
     */
    @Override
    public ListIterator<E> listIterator() {
        return new AionListIterator(0);
    }

    /**
     * Returns a list iterator over the elements in this list,
     * starting at the specified position in the list.
     *
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        rangeCheckForAdd(index);
        return new AionListIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * The semantics of the list returned by this method become undefined if
     * structural changes are made to the backing list, i.e not through the
     * returned list.
     *
     * Structural modifications are those that change the size of this list.
     *
     * @throws IndexOutOfBoundsException  if index is out of range
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);

        return new AionSubList<>(this, fromIndex, toIndex);
    }

    private static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }

    /**
     * Returns an iterator over the elements in this list.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<E> iterator() {
        return new AionListIterator(0);
    }

    public class AionListIterator implements ListIterator<E> {
        private int lastReturnedIndex = -1;
        private int nextIndex;
        int expectedModCount = modCount;

        public AionListIterator(int nextIndex) {
            this.nextIndex = nextIndex;
        }
        @Override
        public boolean hasNext() {
            return this.nextIndex < AionList.this.size;
        }
        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            checkForComodification();
            E elt = null;
            if (hasNext()) {
                elt = (E) AionList.this.storage[this.nextIndex];
                lastReturnedIndex = this.nextIndex;
                this.nextIndex += 1;
            } else {
                throw new NoSuchElementException();
            }
            return elt;
        }
        @Override
        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }
        @SuppressWarnings("unchecked")
        @Override
        public E previous() {
            checkForComodification();
            E elt = null;
            if (hasPrevious()) {
                this.nextIndex -= 1;
                elt = (E) AionList.this.storage[this.nextIndex];
                lastReturnedIndex = this.nextIndex;
            } else {
                throw new NoSuchElementException();
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
            if (this.lastReturnedIndex < 0) {
                throw new IllegalStateException();
            } else {
                checkForComodification();
                AionList.this.remove(this.lastReturnedIndex);
                this.nextIndex = this.lastReturnedIndex;
                this.lastReturnedIndex = -1;
                expectedModCount = modCount;
            }
        }
        @Override
        public void set(E e) {
            if (this.lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }
            // since the size of the list is not changed, modCount is not incremented
            AionList.this.set(this.lastReturnedIndex, e);
        }
        @Override
        public void add(E e) {
            checkForComodification();
            AionList.this.add(this.nextIndex, e);
            this.lastReturnedIndex = -1;
            this.nextIndex += 1;
            expectedModCount = modCount;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new RuntimeException();
            }
        }
    }

    private static class AionSubList<E> implements List<E> {

        private final AionList<E> root;
        private final AionSubList<E> parent;
        private final int offset;
        private int size;
        private int modCount;

        /**
         * Constructs a sublist from given root AionList
         */
        public AionSubList(AionList<E> root, int fromIndex, int toIndex) {
            this.root = root;
            this.parent = null;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }

        /**
         * Constructs a sublist of another SubList.
         */
        private AionSubList(AionSubList<E> parent, int fromIndex, int toIndex) {
            this.root = parent.root;
            this.parent = parent;
            this.offset = parent.offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = root.modCount;
        }

        @Override
        public int size() {
            checkForComodification();
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            checkForComodification();
            return 0 == this.size;
        }

        @Override
        public boolean contains(Object toFind) {
            checkForComodification();
            return indexOf(toFind) >= 0;
        }

        @Override
        public Iterator<E> iterator() {
            return listIterator(0);
        }

        @Override
        public Object[] toArray() {
            checkForComodification();
            Object[] ret = new Object[this.size];
            System.arraycopy(root.storage, offset, ret, 0, this.size);
            return ret;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(E newElement) {
            checkForComodification();
            root.add(offset + this.size, newElement);
            updateSizeAndModCount(1);
            return true;
        }

        @Override
        public boolean remove(Object element) {
            checkForComodification();
            boolean ret = false;
            int indexToRemove = indexOf(element);
            if (indexToRemove >= 0) {
                root.remove(indexToRemove + offset);
                updateSizeAndModCount(-1);
                ret = true;
            }
            return ret;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object obj: c){
                if (!this.contains(obj)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0) {
                return false;
            }
            checkForComodification();
            this.root.addAll(index + offset, c);
            updateSizeAndModCount(cSize);
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean ret = false;
            for(Object obj : c){
                boolean removed = remove(obj);
                if(removed){
                    ret = true;
                }
            }
            return ret;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            checkForComodification();
            Iterator<E> it = this.listIterator(0);
            boolean ret = false;

            while(it.hasNext()){
                if (!c.contains(it.next())){
                    it.remove();
                    ret = true;
                }
            }
            return ret;
        }

        @Override
        public void clear() {
            checkForComodification();
            for (int i = size - 1; i >= 0; i--) {
                root.remove(i + offset);
            }
            updateSizeAndModCount(-size);
        }

        @Override
        public E get(int index) {
            checkIndex(index);
            checkForComodification();
            return root.get(index + offset);
        }

        @Override
        public E set(int index, E element) {
            checkIndex(index);
            checkForComodification();
            return root.set(offset + index, element);
        }

        @Override
        public void add(int index, E element) {
            rangeCheckForAdd(index);
            checkForComodification();
            root.add(index + offset, element); 
            updateSizeAndModCount(1);
        }

        @Override
        public E remove(int index) {
            checkIndex(index);
            checkForComodification();
            E result = root.remove(index + offset);
            updateSizeAndModCount(-1);
            return result;
        }

        @Override
        public int indexOf(Object o) {
            checkForComodification();
            int end = this.size + this.offset;
            if (o == null) {
                for (int i = this.offset; i < end; i++) {
                    if (root.storage[i] == null) {
                        return i - this.offset;
                    }
                }
            } else {
                for (int i = this.offset; i < end; i++) {
                    if (o.equals(root.storage[i])) {
                        return i - this.offset;
                    }
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            checkForComodification();
            int start = this.size + this.offset - 1;
            if (o == null) {
                for (int i = start; i >= this.offset; i--) {
                    if (root.storage[i] == null) {
                        return i - this.offset;
                    }
                }
            } else {
                for (int i = start; i >= this.offset; i--) {
                    if (o.equals(root.storage[i])) {
                        return i - this.offset;
                    }
                }
            }
            return -1;
        }

        @Override
        public ListIterator<E> listIterator() {
            return new AionSubListIterator(0);
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return new AionSubListIterator(index);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new AionSubList<>(this, fromIndex, toIndex);
        }

        /**
         * Helpers
         */

        private void checkForComodification() {
            if (root.modCount != modCount) {
                throw new RuntimeException();
            }
        }

        private void updateSizeAndModCount(int sizeChange) {
            AionSubList<E> slist = this;
            do {
                slist.size += sizeChange;
                slist.modCount = root.modCount;
                slist = slist.parent;
            } while (slist != null);
        }

        private void rangeCheckForAdd(int index) {
            if (index > size || index < 0) {
                throw new IndexOutOfBoundsException();
            }
        }

        private void checkIndex(int index) {
            if (index >= size || index < 0) {
                throw new IndexOutOfBoundsException();
            }
        }

        public class AionSubListIterator implements ListIterator<E> {
            ListIterator<E> itr;

            public AionSubListIterator(int index) {
                checkForComodification();
                rangeCheckForAdd(index);
                itr = root.listIterator(offset + index);
            }

            public boolean hasNext() {
                return nextIndex() < AionSubList.this.size;
            }

            public E next() {
                checkForComodification();
                if (hasNext()) {
                    return itr.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                checkForComodification();
                if (hasPrevious()) {
                    return itr.previous();
                } else {
                    throw new NoSuchElementException();
                }
            }

            public int nextIndex() {
                return itr.nextIndex() - offset;
            }

            public int previousIndex() {
                return itr.previousIndex() - offset;
            }

            public void remove() {
                checkForComodification();
                itr.remove();
                updateSizeAndModCount(-1);
            }

            public void set(E e) {
                itr.set(e);
            }

            public void add(E e) {
                checkForComodification();
                itr.add(e);
                updateSizeAndModCount(1);
            }
        }
    }
}
