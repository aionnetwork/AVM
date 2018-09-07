package org.aion.avm.userlib;

import java.util.*;

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
        for (int i = 0; i < size; i = i + 2) {
            map.keySet().remove(i);
            Assert.assertFalse(map.containsKey(i));
        }
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
     * Adds 20 elements, remove entrys from its collection views
     */
    @Test
    public void checkCollectionViews(){
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }

        Assert.assertEquals(size, map.size());

        Set<Integer> ks = map.keySet();
        Collection<String> vs = map.values();
        Set<Map.Entry<Integer, String>> es = map.entrySet();

        Assert.assertEquals(ks.size(), map.size());
        Assert.assertEquals(vs.size(), map.size());
        Assert.assertEquals(es.size(), map.size());

        // Remove from map, check views
        map.remove(10);
        Assert.assertTrue(!ks.contains(10));
        Assert.assertTrue(!vs.contains("int_ " + 10));
        Assert.assertTrue(es.size() == 19);

        // Remove from views, check map
        ks.remove(15);
        Assert.assertTrue(!map.containsKey(15));
        Assert.assertTrue(!vs.contains("int_ " + 15));
        Assert.assertTrue(es.size() == 18);

        vs.remove("int_ " + 16);
        Assert.assertTrue(!map.containsKey(16));
        Assert.assertTrue(!ks.contains(16));
        Assert.assertTrue(es.size() == 17);

        es.clear();
        Assert.assertEquals(es.size(), 0);
        Assert.assertEquals(vs.size(), 0);
        Assert.assertEquals(map.size(), 0);
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
     * Adds 20 elements, use key set iterator to remove half of them.
     */
    @Test
    public void keySetIteratorRemove() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }

        Assert.assertEquals(size, map.size());

        Iterator<Integer> i = map.keySet().iterator();
        while (i.hasNext()) {
            i.next();
            i.next();
            i.remove();
        }

        Assert.assertEquals(size / 2, map.size());

        for (int j = 0; j < size; j = j + 1) {
            if (j % 2 == 0) {
                Assert.assertEquals(map.get(j), "int_ " + j);
            }else{
                Assert.assertTrue(null == map.get(j));
            }
        }
    }

    /**
     * Adds 20 elements, use entry set iterator to remove half of them.
     */
    @Test
    public void entrySetIteratorRemove() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }

        Assert.assertEquals(size, map.size());

        Iterator<Map.Entry<Integer, String>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            i.next();
            i.next();
            i.remove();
        }

        Assert.assertEquals(size / 2, map.size());

        for (int j = 0; j < size; j = j + 1) {
            if (j % 2 == 0) {
                Assert.assertEquals(map.get(j), "int_ " + j);
            }else{
                Assert.assertTrue(null == map.get(j));
            }
        }
    }

    /**
     * Adds 20 elements, use values iterator to remove half of them.
     */
    @Test
    public void valuesIteratorRemove() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            map.put(i, "int_ " + i);
        }

        Assert.assertEquals(size, map.size());

        Iterator<String> i = map.values().iterator();
        while (i.hasNext()) {
            i.next();
            i.next();
            i.remove();
        }

        Assert.assertEquals(size / 2, map.size());

        for (int j = 0; j < size; j = j + 1) {
            if (j % 2 == 0) {
                Assert.assertEquals(map.get(j), "int_ " + j);
            }else{
                Assert.assertTrue(null == map.get(j));
            }
        }
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
