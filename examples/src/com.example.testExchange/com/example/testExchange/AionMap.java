package com.example.testExchange;


/**
 * The first rough cut of the Map-like abstraction we are providing to our user-space apps.
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
public class AionMap<K, V> {
    private static final int kStartSize = 1;

    private Object[] keys;
    private Object[] values;

    public AionMap() {
        this.keys = new Object[kStartSize];
        this.values = new Object[kStartSize];
    }

    public void put(K key, V value) {
        // This implementation is very simple so we just walk the list, seeing if this is already here.
        int insertIndex = this.keys.length;
        for (int i = 0; i < this.keys.length; ++i) {
            Object elt = this.keys[i];
            if (null == elt) {
                // The data is always packed into the beginning so the first hole is the end.
                insertIndex = i;
                break;
            } else {
                if (key.equals(elt)) {
                    // Over-write this.
                    insertIndex = i;
                    break;
                } else {
                    // Keep searching.
                }
            }
        }
        
        if (insertIndex >= 0) {
            if (insertIndex >= this.keys.length) {
                // Grow.
                Object[] newKeys = new Object[this.keys.length * 2];
                Object[] newValues = new Object[this.values.length * 2];
                for (int i = 0; i < this.keys.length; ++i) {
                    newKeys[i] = this.keys[i];
                    newValues[i] = this.values[i];
                }
                this.keys = newKeys;
                this.values = newValues;
            }
            // Now, insert.
            this.keys[insertIndex] = key;
            this.values[insertIndex] = value;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        V found = null;
        for (int i = 0; (null == found) && (i < this.keys.length) && (null != this.keys[i]); ++i) {
            if (key.equals(this.keys[i])) {
                found = (V) this.values[i];
            }
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    public V remove(K toRemove) {
        int foundIndex = -1;
        for (int i = 0; (-1 == foundIndex) && (i < this.keys.length) && (null != this.keys[i]); ++i) {
            if (toRemove.equals(this.keys[i])) {
                foundIndex = i;
            }
        }
        
        V removed = null;
        if (-1 != foundIndex) {
            removed = (V) this.values[foundIndex];
            for (int i = foundIndex; i < this.keys.length; ++i) {
                Object toReadKey = ((i + 1) < this.keys.length)
                        ? this.keys[i+1]
                        : null;
                this.keys[i] = toReadKey;
                
                Object toReadValue = ((i + 1) < this.values.length)
                        ? this.values[i+1]
                        : null;
                this.values[i] = toReadValue;
            }
        }
        return removed;
    }

    public int size() {
        int size = 0;
        for (Object elt : this.keys) {
            if (null != elt) {
                size += 1;
            } else {
                break;
            }
        }
        return size;
    }

    public void clear() {
        this.keys = new Object[kStartSize];
        this.values = new Object[kStartSize];
    }

    public Object[] getKeys() {
        return keys;
    }

    public boolean containsKey(K key){
        return (this.get(key) != null);
    }
}
