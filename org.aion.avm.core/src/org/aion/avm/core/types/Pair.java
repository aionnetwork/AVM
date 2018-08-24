package org.aion.avm.core.types;

public class Pair<K ,V> {
    public K key;
    public V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}
