package org.aion.avm.userlib;


/**
 * The first rough cut of the Set-like abstraction we are providing to our user-space apps.
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
public class AionSet<E> {
    private static final int kStartSize = 1;

    private Object[] storage;

    public AionSet() {
        this.storage = new Object[kStartSize];
    }

    public void add(E newElement) {
        // This implementation is very simple so we just walk the list, seeing if this is already here.
        int insertIndex = this.storage.length;
        for (int i = 0; i < this.storage.length; ++i) {
            Object elt = this.storage[i];
            if (null == elt) {
                // The data is always packed into the beginning so the first hole is the end.
                insertIndex = i;
                break;
            } else {
                if (newElement.equals(elt)) {
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
                Object[] newStorage = new Object[this.storage.length * 2];
                for (int i = 0; i < this.storage.length; ++i) {
                    newStorage[i] = this.storage[i];
                }
                this.storage = newStorage;
            }
            // Now, insert.
            this.storage[insertIndex] = newElement;
        }
    }

    public boolean contains(E check) {
        boolean doesContain = false;
        for (int i = 0; !doesContain && (i < this.storage.length) && (null != this.storage[i]); ++i) {
            doesContain = check.equals(this.storage[i]);
        }
        return doesContain;
    }

    public boolean remove(E toRemove) {
        int foundIndex = -1;
        for (int i = 0; (-1 == foundIndex) && (i < this.storage.length) && (null != this.storage[i]); ++i) {
            if (toRemove.equals(this.storage[i])) {
                foundIndex = i;
            }
        }
        
        if (-1 != foundIndex) {
            for (int i = foundIndex; i < this.storage.length; ++i) {
                Object toRead = ((i + 1) < this.storage.length)
                        ? this.storage[i+1]
                        : null;
                this.storage[i] = toRead;
            }
        }
        return (-1 != foundIndex);
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
}
