package org.aion.avm.userlib;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class AionMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_CAPACITY = 1;

    private Object[] keys;
    private Object[] values;

    private int size;

    public AionMap() {
        this.keys = new Object[DEFAULT_CAPACITY];
        this.values = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public V put(K key, V value) {
        boolean newEntry = false;
        // This implementation is very simple so we just walk the list, seeing if this is already here.
        int insertIndex = this.keys.length;
        for (int i = 0; i < this.keys.length; ++i) {
            Object elt = this.keys[i];
            if (null == elt) {
                // The data is always packed into the beginning so the first hole is the end.
                insertIndex = i;
                newEntry = true;
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
                newEntry = true;
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

            if (newEntry) {size = size + 1;}
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V found = null;
        for (int i = 0; (null == found) && (i < this.keys.length) && (null != this.keys[i]); ++i) {
            if (key.equals(this.keys[i])) {
                found = (V) this.values[i];
            }
        }
        return found;
    }

    public V getOrDefault(Object key, V defaultValue) {
        return containsKey(key) ? get(key) : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public V remove(Object toRemove) {
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
            size = size - 1;
        }
        return removed;
    }

    public int size() {
        return this.size;
    }

    public void clear() {
        this.keys = new Object[DEFAULT_CAPACITY];
        this.values = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public Object[] getKeys() {
        return keys;
    }

    public boolean containsKey(Object key){
        return (this.get(key) != null);
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < this.size; i++){
            if (this.values[i].equals(value)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Set<?> ks = m.keySet();
        for (Object key : ks){
            Object value = m.get(key);
            this.put((K) key, (V) value);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> ret = new AionSet<>();
        for (int i = 0; i < size; i++){
            ret.add((K) this.keys[i]);
        }
        return ret;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        Collection<V> ret = new AionList<>();
        for (int i = 0; i < size; i++){
            ret.add((V) this.values[i]);
        }
        return ret;
    }
}
