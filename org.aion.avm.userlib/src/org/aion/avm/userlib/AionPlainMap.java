package org.aion.avm.userlib;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * The first rough cut of the Map-like abstraction we are providing to our user-space apps.
 * Note:  This implementation depends on keys having a sensible hashCode() implementation.
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
public class AionPlainMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_CAPACITY = 1;

    // To avoid faulting in all the keys in storage, we cache their hashes to see if it is even worth checking.
    private int[] hashes;
    private Object[] keys;
    private Object[] values;

    private int size;

    public AionPlainMap() {
        this.hashes = new int[DEFAULT_CAPACITY];
        this.keys = new Object[DEFAULT_CAPACITY];
        this.values = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public V put(K key, V value) {
        int keyHash = key.hashCode();
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
                // Check the hash cache, first (equals() will fault in the object).
                if ((keyHash == this.hashes[i]) && key.equals(elt)) {
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
                int[] newHashes = new int[this.hashes.length * 2];
                Object[] newKeys = new Object[this.keys.length * 2];
                Object[] newValues = new Object[this.values.length * 2];
                System.arraycopy(this.hashes, 0, newHashes, 0, this.hashes.length);
                System.arraycopy(this.keys, 0, newKeys, 0, this.keys.length);
                System.arraycopy(this.values, 0, newValues, 0, this.values.length);
                this.hashes = newHashes;
                this.keys = newKeys;
                this.values = newValues;
            }
            // Now, insert.
            this.hashes[insertIndex] = keyHash;
            this.keys[insertIndex] = key;
            this.values[insertIndex] = value;

            if (newEntry) {size = size + 1;}
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        int keyHash = key.hashCode();
        V found = null;
        for (int i = 0; (null == found) && (i < this.keys.length) && (null != this.keys[i]); ++i) {
            // Check the hash cache, first (equals() will fault in the object).
            if ((keyHash == this.hashes[i]) && key.equals(this.keys[i])) {
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
        int keyHash = toRemove.hashCode();
        int foundIndex = -1;
        for (int i = 0; (-1 == foundIndex) && (i < this.keys.length) && (null != this.keys[i]); ++i) {
            // Check the hash cache, first (equals() will fault in the object).
            if ((keyHash == this.hashes[i]) && toRemove.equals(this.keys[i])) {
                foundIndex = i;
            }
        }
        
        V removed = null;
        if (-1 != foundIndex) {
            removed = (V) this.values[foundIndex];
            
            // We have the size so we can just shift these with arraycopy (and over-write the last entry).
            int newSize = this.size - 1;
            System.arraycopy(this.hashes, 1, this.hashes, 0, newSize);
            this.hashes[newSize] = 0;
            System.arraycopy(this.keys, 1, this.keys, 0, newSize);
            this.keys[newSize] = null;
            System.arraycopy(this.values, 1, this.values, 0, newSize);
            this.values[newSize] = null;
            
            this.size = newSize;
        }
        return removed;
    }

    public int size() {
        return this.size;
    }

    public void clear() {
        this.hashes = new int[DEFAULT_CAPACITY];
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
            // (we can't use the hash since we only apply that to the key - we don't require values have a sensible hashCode()).
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
        // WARNING:  This returns a copy of the key set, meaning that modifications to that set won't reflect in the underlying map
        // as the interface claims it should.
        Set<K> ret = new AionPlainSet<>();
        for (int i = 0; i < size; i++){
            ret.add((K) this.keys[i]);
        }
        return ret;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // WARNING:  This returns a copy of the key set, meaning that modifications to that set won't reflect in the underlying map
        // as the interface claims it should.
        Set<Entry<K, V>> ret = new AionPlainSet<>();
        for (int i = 0; i < size; i++){
            ret.add(new AionMapEntry<K, V>((K) this.keys[i], (V) this.values[i]));
        }
        return ret;
    }

    @Override
    public Collection<V> values() {
        Collection<V> ret = new AionList<>();
        for (int i = 0; i < size; i++){
            ret.add((V) this.values[i]);
        }
        return ret;
    }


    public static class AionMapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;
        public AionMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public K getKey() {
            return this.key;
        }
        @Override
        public V getValue() {
            return this.value;
        }
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}
