package org.aion.avm.core.util;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A simple concurrent cache, based on SoftReferences.  There is currently no maximum size.
 * NOTE:  Keys in this implementation are NOT cleaned up when the values are cleared.  We could change this, in the future, by introducing
 * a clearing thread.
 *
 * @param <K> The key type (should have sensible hashCode() and equals() implementations).
 * @param <V> The value type.
 */
public class SoftCache<K, V> {
    private final ConcurrentHashMap<K, SoftReference<V>> underlyingMap;

    public SoftCache() {
        this.underlyingMap = new ConcurrentHashMap<>();
    }

    public V checkout(K key) {
        SoftReference<V> wrapper = this.underlyingMap.remove(key);
        return (null != wrapper)
                ? wrapper.get()
                : null;
    }

    public void checkin(K key, V value) {
        SoftReference<V> previous = this.underlyingMap.put(key, new SoftReference<>(value));
        // We don't expect collisions in this cache - that would imply that consumers disagree about cache state.
        // (in the future, we probably want to change this).
        Assert.assertNull(previous);
    }
}
