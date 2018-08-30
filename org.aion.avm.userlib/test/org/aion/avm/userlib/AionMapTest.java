package org.aion.avm.userlib;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


public class AionMapTest {
    /**
     * Creates an empty map, checks its size, and verifies we can't read or remove from it.
     */
    @Test
    public void emptyMapTest() {
        AionMap<Integer, Void> map = new AionMap<>();
        Assert.assertEquals(0, map.size());
        Assert.assertEquals(null, map.get(Integer.valueOf(4)));
        Assert.assertEquals(null, map.remove(Integer.valueOf(5)));
    }

    /**
     * Adds 100 elements, forcing growth, and then tries interacting with the map.
     */
    @Test
    public void addManyElements() {
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < 100; ++i) {
            map.put(i, "int_ " + i);
        }
        Assert.assertEquals(100, map.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals("int_ " + i, map.get(i));
        }
        Assert.assertEquals(100, map.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals("int_ " + i, map.remove(i));
        }
        Assert.assertEquals(0, map.size());
    }

    /**
     * Adds the same 20 elements, over and over, verifying that we only get the last version.
     */
    @Test
    public void addDuplicates() {
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 20; ++j) {
                map.put(j, "int_ " + i);
            }
        }
        Assert.assertEquals(20, map.size());
        for (int j = 0; j < 20; ++j) {
            Assert.assertEquals("int_ 9", map.remove(j));
        }
        Assert.assertEquals(0, map.size());
    }

    /**
     * Adds 10 elements, then walks the key set.
     */
    @Test
    public void checkKeySet() {
        final int size = 10;
        boolean[] markMap = new boolean[size];
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }
        Assert.assertEquals(size, map.size());
        int found = 0;
        for (int key : map.keySet()) {
            Assert.assertFalse(markMap[key]);
            markMap[key] = true;
            found += 1;
        }
        Assert.assertEquals(size, found);
    }

    /**
     * Adds 10 elements, then walks the values collection.
     */
    @Test
    public void checkValues() {
        final int size = 10;
        Set<String> foundSet = new HashSet<>();
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }
        Assert.assertEquals(size, map.size());
        for (String value : map.values()) {
            Assert.assertFalse(foundSet.contains(value));
            foundSet.add(value);
        }
        Assert.assertEquals(size, foundSet.size());
    }

    /**
     * Adds 10 elements, then walks the entry set.
     */
    @Test
    public void checkEntrySet() {
        final int size = 10;
        boolean[] markMap = new boolean[size];
        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }
        Assert.assertEquals(size, map.size());
        int found = 0;
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            int key = entry.getKey();
            Assert.assertFalse(markMap[key]);
            markMap[key] = true;
            found += 1;
        }
        Assert.assertEquals(size, found);
    }

    /**
     * Adds 20 elements, using only 10 unique hashes, and then makes sure that all of the keys are still there.
     * This verifies that equals()-comparison is ultimately used to find duplicate keys, not depending on unique hashCode(), alone.
     */
    @Test
    public void addElementsWithCollidingHashes() {
        final int size = 20;
        final int hashCount = 10;
        AionMap<TestElement, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            TestElement elt = new TestElement(i % hashCount, i);
            map.put(elt, elt.toString());
        }
        Assert.assertEquals(size, map.size());
        
        boolean[] markMap = new boolean[size];
        int[] hashes = new int[hashCount];
        int found = 0;
        for (Map.Entry<TestElement, String> entry : map.entrySet()) {
            TestElement key = entry.getKey();
            Assert.assertFalse(markMap[key.value]);
            markMap[key.value] = true;
            hashes[key.hash] += 1;
            Assert.assertEquals(entry.getValue(), key.toString());
            found += 1;
        }
        Assert.assertEquals(size, found);
        for (int hash : hashes) {
            // We should see 2 of each.
            Assert.assertEquals(2, hash);
        }
    }
}
