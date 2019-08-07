package org.aion.avm.userlib;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;


public class AionMapTest {
    private static int entryCount = 48;

    @Test
    public void putTest() {
        AionMap<Integer, String> map = new AionMap<>();
        int count = 5;
        for (int i = 0; i < count; i++) {
            int key = createCollisionKey(i);
            Assert.assertNull(map.put(key, String.valueOf(i)));
            Assert.assertEquals(i + 1, map.size());
            Assert.assertEquals(String.valueOf(i), map.get(key));
        }

        //rewrite
        for (int i = 0; i < count; i++) {
            int key = createCollisionKey(i);
            Assert.assertEquals(String.valueOf(i), map.put(key, String.valueOf(key)));
            Assert.assertEquals(count, map.size());
            Assert.assertEquals(String.valueOf(key), map.get(key));
        }
    }

    @Test
    public void putAllTest() {
        AionMap<Integer, String> temp = new AionMap<>();
        int count = 256;
        for (int i = 0; i < count; i++) {
            temp.put(i, String.valueOf(i));
        }

        AionMap<Integer, String> map = new AionMap<>();
        map.putAll(temp);
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(String.valueOf(i), temp.get(i));
        }
    }

    @Test
    public void removeTest() {
        AionMap<Integer, String> map = new AionMap<>();
        int count = 10;
        List<Integer> keysToRemove = Arrays.asList(createCollisionKey(0), createCollisionKey(2), createCollisionKey(4), createCollisionKey(count - 1));

        for (int i = 0; i < count; i++) {
            int key = createCollisionKey(i);
            map.put(key, String.valueOf(key));
        }

        Assert.assertEquals(count, map.size());

        for (int i = 0; i < keysToRemove.size(); i++) {
            int key = keysToRemove.get(i);
            Assert.assertEquals(String.valueOf(key), map.remove(key));
            Assert.assertNull(map.remove(key));
            Assert.assertNull(map.get(key));
            Assert.assertEquals(count - i - 1, map.size());
            Assert.assertFalse(map.containsKey(key));
        }

        for (int i = 0; i < count; i++) {
            int k = createCollisionKey(i);
            if (keysToRemove.contains(k)) {
                Assert.assertNull(map.get(k));
            } else {
                Assert.assertEquals(String.valueOf(k), map.get(k));
            }
        }

        // null value
        int nullKey = 17;
        Assert.assertNull(map.put(nullKey, null));
        Assert.assertNull(map.remove(nullKey));
    }

    @Test
    public void containsTest() {
        AionMap<Integer, String> map = new AionMap<>();

        for (int i = 0; i < entryCount - 1; i++) {
            map.put(i, String.valueOf(i * 100));
        }

        map.put(entryCount, null);
        Assert.assertTrue(map.containsValue(null));

        Assert.assertTrue(map.containsValue(null));
        Assert.assertTrue(map.containsKey(entryCount));

        for (int i = 0; i < entryCount - 1; i++) {
            Assert.assertTrue(map.containsKey(i));
            Assert.assertTrue(map.containsValue(String.valueOf(i * 100)));
        }
    }

    @Test
    public void sizeTest() {
        AionMap<Integer, String> map = new AionMap<>();

        for (int i = 0; i < entryCount; i++) {
            map.put(i, String.valueOf(i * 100));
            Assert.assertEquals(i + 1, map.size());
            Assert.assertEquals(i + 1, map.entrySet().size());
            Assert.assertEquals(i + 1, map.keySet().size());
            Assert.assertEquals(i + 1, map.values().size());
        }

        for (int i = 0; i < entryCount; i++) {
            map.remove(i);
            Assert.assertEquals(entryCount - i - 1, map.size());
            Assert.assertEquals(entryCount - i - 1, map.entrySet().size());
            Assert.assertEquals(entryCount - i - 1, map.keySet().size());
            Assert.assertEquals(entryCount - i - 1, map.values().size());
        }
    }

    @Test
    public void entrySetIteratorTest() {
        AionMap<Integer, Integer> map = new AionMap<>();
        for (int i = 0; i < entryCount; i++) {
            map.put(i, i * 100);
        }

        //validate iterator values
        Iterator iterator = map.entrySet().iterator();
        for (int i = 0; i < entryCount; i++) {
            Assert.assertTrue(iterator.hasNext());
            Map.Entry e = (Map.Entry) iterator.next();
            Assert.assertEquals(i, e.getKey());
            Assert.assertEquals(i * 100, e.getValue());
            Assert.assertTrue(map.entrySet().contains(e));
        }

        // null value
        iterator = map.entrySet().iterator();
        map.put(0, null);
        Map.Entry entry = (Map.Entry) iterator.next();
        Assert.assertEquals(0, entry.getKey());
        Assert.assertNull(entry.getValue());
        Assert.assertTrue(map.entrySet().contains(entry));

        //remove and validate
        entry = (Map.Entry) iterator.next();
        iterator.remove();

        Assert.assertNull(map.get(1));
        Assert.assertFalse(map.entrySet().contains(entry));
        Assert.assertEquals(entryCount - 1, map.size());
        Assert.assertEquals(entryCount - 1, map.entrySet().size());

        //validate next entry can be retrieved correctly
        entry = (Map.Entry) iterator.next();
        Assert.assertEquals(2, entry.getKey());
        Assert.assertEquals(2 * 100, entry.getValue());
        Assert.assertTrue(map.entrySet().contains(entry));
    }

    @Test
    public void entrySetIteratorExceptionTest() {
        AionMap<Integer, Integer> map = new AionMap<>();
        map.put(3, 300);
        map.put(6, 600);

        //validate can find the first value correctly
        Iterator iterator = map.entrySet().iterator();
        Assert.assertTrue(iterator.hasNext());
        Map.Entry e = (Map.Entry) iterator.next();
        Assert.assertEquals(3, e.getKey());

        //should not throw an exception
        map.put(6, 601);
        Assert.assertTrue(iterator.hasNext());
        e = (Map.Entry) iterator.next();
        Assert.assertEquals(6, e.getKey());
        Assert.assertEquals(601, e.getValue());

        // reading when element is not there
        boolean exceptionThrown = false;
        try {
            e = (Map.Entry) iterator.next();
        } catch (NoSuchElementException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // reading after a new entry has been put
        iterator = map.entrySet().iterator();
        map.put(1, 100);
        exceptionThrown = false;
        try {
            e = (Map.Entry) iterator.next();
        } catch (RuntimeException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // reading when an entry has been removed
        iterator = map.entrySet().iterator();
        map.remove(6);
        exceptionThrown = false;
        try {
            e = (Map.Entry) iterator.next();
        } catch (RuntimeException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void keySetIteratorTest() {
        AionMap<Integer, Integer> map = new AionMap<>();
        for (int i = 0; i < entryCount; i++) {
            map.put(i, i * 100);
        }

        Iterator iterator = map.keySet().iterator();
        for (int i = 0; i < entryCount; i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(i, (int) iterator.next());
            Assert.assertTrue(map.keySet().contains(i));
        }

        //remove and validate
        iterator = map.keySet().iterator();
        int k = (int) iterator.next();
        iterator.remove();

        Assert.assertNull(map.get(k));
        Assert.assertFalse(map.keySet().contains(k));
        Assert.assertEquals(entryCount - 1, map.size());
        Assert.assertEquals(entryCount - 1, map.keySet().size());

        //validate next entry can be retrieved correctly
        k = (int) iterator.next();
        Assert.assertNotNull(map.get(k));
        Assert.assertTrue(map.keySet().contains(k));
    }

    @Test
    public void valuesIteratorTest() {
        AionMap<Integer, Integer> map = new AionMap<>();
        for (int i = 0; i < entryCount; i++) {
            map.put(i, i * 100);
        }

        Iterator iterator = map.values().iterator();
        for (int i = 0; i < entryCount; i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(i * 100, (int) iterator.next());
            Assert.assertTrue(map.values().contains(i * 100));
        }

        // null value
        iterator = map.values().iterator();
        map.put(0, null);
        Assert.assertNull(iterator.next());
        Assert.assertTrue(map.values().contains(null));

        //remove and validate
        int v = (int) iterator.next();
        iterator.remove();

        Assert.assertNull(map.get(1));
        Assert.assertFalse(map.values().contains(v));
        Assert.assertEquals(entryCount - 1, map.size());
        Assert.assertEquals(entryCount - 1, map.values().size());

        //validate next entry can be retrieved correctly
        v = (int) iterator.next();
        Assert.assertEquals(2 * 100, v);
        Assert.assertTrue(map.values().contains(v));

    }

    @Test
    public void valuesCollectionViewTest() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; i++) {
            map.put(i, String.valueOf(i));
        }

        Assert.assertEquals(size, map.size());

        Collection<String> values = map.values();

        //size
        Assert.assertEquals(size, values.size());

        //isEmpty
        Assert.assertFalse(values.isEmpty());

        //contains
        for (int i = 0; i < size; i++) {
            Assert.assertTrue(values.contains(String.valueOf(i)));
        }
        //null value
        map.put(1, null);
        Assert.assertTrue(values.contains(null));

        //containsAll
        ArrayList<String> sampleValues = new ArrayList<>(Arrays.asList("5", "6", "10", "15"));
        Assert.assertTrue(values.containsAll(sampleValues));
        sampleValues.add(String.valueOf(size + 10));
        Assert.assertFalse(values.containsAll(sampleValues));

        //retainAll
        sampleValues.remove(String.valueOf(size + 10));

        Assert.assertTrue(values.retainAll(sampleValues));
        Assert.assertEquals(sampleValues.size(), values.size());
        for (int i = 0; i < size; i++) {
            if (sampleValues.contains(String.valueOf(i))) {
                Assert.assertTrue(values.contains(String.valueOf(i)));
                Assert.assertTrue(map.containsValue(String.valueOf(i)));
            } else {
                Assert.assertFalse(values.contains(String.valueOf(i)));
                Assert.assertFalse(map.containsValue(String.valueOf(i)));
            }
        }

        //remove
        Assert.assertTrue(values.remove("5"));
        Assert.assertFalse(values.remove(String.valueOf(size + 10)));
        Assert.assertEquals(sampleValues.size() - 1, values.size());
        Assert.assertFalse(values.contains("5"));
        Assert.assertFalse(map.containsValue("5"));

        //removeAll
        List<String> removedValues = Arrays.asList("5", "6");
        sampleValues.removeAll(removedValues);
        Assert.assertTrue(values.removeAll(removedValues));
        Assert.assertEquals(sampleValues.size(), values.size());
        for (int i = 0; i < size; i++) {
            if (sampleValues.contains(String.valueOf(i))) {
                Assert.assertTrue(values.contains(String.valueOf(i)));
                Assert.assertTrue(map.containsValue(String.valueOf(i)));
            } else {
                Assert.assertFalse(values.contains(String.valueOf(i)));
                Assert.assertFalse(map.containsValue(String.valueOf(i)));
            }
        }

        //clear
        values.clear();
        Assert.assertEquals(0, values.size());
        Assert.assertEquals(0, map.size());

        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void entrySetCollectionViewTest() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        AionMap<Integer, String> copy = new AionMap<>();

        for (int i = 0; i < size; i++) {
            map.put(i, String.valueOf(i));
            if (i % 5 == 0)
                copy.put(i, String.valueOf(i));
        }

        Assert.assertEquals(size, map.size());

        Set<Map.Entry<Integer, String>> entries = map.entrySet();

        //size
        Assert.assertEquals(size, entries.size());

        //isEmpty
        Assert.assertFalse(entries.isEmpty());

        //contains
        for (Map.Entry<Integer, String> e : copy.entrySet()) {
            Assert.assertTrue(entries.contains(e));
        }

        //containsAll
        Assert.assertTrue(entries.containsAll(copy.entrySet()));

        //retainAll
        Assert.assertTrue(entries.retainAll(copy.entrySet()));
        Assert.assertEquals(copy.size(), entries.size());
        for (Map.Entry<Integer, String> e : copy.entrySet()) {
            Assert.assertTrue(entries.contains(e));
        }
        for (int i = 0; i < size; i++) {
            if (!copy.containsKey(i)) {
                Assert.assertFalse(map.containsKey(i));
                Assert.assertFalse(map.containsValue(String.valueOf(i)));
            }
        }

        //remove
        Iterator iterator = copy.entrySet().iterator();
        Map.Entry<Integer, String> entry = (Map.Entry) iterator.next();
        Assert.assertTrue(entries.remove(entry));
        Assert.assertEquals(copy.size() - 1, entries.size());
        Assert.assertFalse(entries.contains(entry));
        Assert.assertFalse(map.containsValue(entry.getValue()));
        Assert.assertFalse(map.containsKey(entry.getKey()));

        //removeAll
        Assert.assertTrue(entries.removeAll(copy.entrySet()));
        Assert.assertEquals(0, entries.size());
        for (Map.Entry<Integer, String> e : copy.entrySet()) {
            Assert.assertFalse(map.entrySet().contains(e));
        }

        //clear
        map.put(1, "String");
        Assert.assertEquals(1, entries.size());

        entries.clear();
        Assert.assertEquals(0, entries.size());
        Assert.assertEquals(0, map.size());

        Assert.assertTrue(entries.isEmpty());
    }

    @Test
    public void keySetCollectionViewTest() {
        final int size = 20;

        AionMap<Integer, String> map = new AionMap<>();
        for (int i = 0; i < size; i++) {
            map.put(i, String.valueOf(i));
        }

        Assert.assertEquals(size, map.size());

        Set<Integer> keySet = map.keySet();

        //size
        Assert.assertEquals(size, keySet.size());

        //isEmpty
        Assert.assertFalse(keySet.isEmpty());

        //contains
        for (int i = 0; i < size; i++) {
            Assert.assertTrue(keySet.contains(i));
        }
        //null value
        boolean exceptionThrown = false;
        try {
            map.put(null, "null");
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        //containsAll
        ArrayList<Integer> sampleKeys = new ArrayList<>(Arrays.asList(5, 6, 10, 15));
        Assert.assertTrue(keySet.containsAll(sampleKeys));
        sampleKeys.add(size + 10);
        Assert.assertFalse(keySet.containsAll(sampleKeys));

        //retainAll
        Assert.assertTrue(keySet.retainAll(sampleKeys));
        Assert.assertEquals(sampleKeys.size() - 1, keySet.size());
        for (int i = 0; i < size; i++) {
            if (sampleKeys.contains(i)) {
                Assert.assertTrue(keySet.contains(i));
                Assert.assertTrue(map.containsKey(i));
            } else {
                Assert.assertFalse(keySet.contains(i));
                Assert.assertFalse(map.containsKey(i));
            }
        }

        sampleKeys.remove(sampleKeys.size() - 1);

        //remove
        Assert.assertTrue(keySet.remove(5));
        Assert.assertFalse(keySet.remove(size + 10));
        Assert.assertEquals(sampleKeys.size() - 1, keySet.size());
        Assert.assertFalse(keySet.contains(5));
        Assert.assertFalse(map.containsKey(5));

        //removeAll
        List<Integer> removedValues = Arrays.asList(5, 6);
        sampleKeys.removeAll(removedValues);
        Assert.assertTrue(keySet.removeAll(removedValues));
        Assert.assertEquals(sampleKeys.size(), keySet.size());
        for (int i = 0; i < size; i++) {
            if (sampleKeys.contains(i)) {
                Assert.assertTrue(keySet.contains(i));
                Assert.assertTrue(map.containsKey(i));
            } else {
                Assert.assertFalse(keySet.contains(i));
                Assert.assertFalse(map.containsKey(i));
            }
        }

        //clear
        keySet.clear();
        Assert.assertEquals(0, keySet.size());
        Assert.assertEquals(0, map.size());

        Assert.assertTrue(keySet.isEmpty());
    }

    @Test
    public void replaceUsingIteratorTest() {
        String key = "key";
        String oldValue = "value_1";
        String newValue = "value_2";
        AionMap<String, String> map = new AionMap();
        map.put(key, oldValue);
        Map.Entry e = map.entrySet().iterator().next();
        Object returnVal = e.setValue(newValue);
        Assert.assertEquals(newValue, map.get(key));
        Assert.assertEquals(oldValue, returnVal);
    }

    @Test
    public void putRemoveHighCollisionTest() {
        AionMap<Integer, String> map = new AionMap<>();

        ArrayList<Integer> keyList = new ArrayList<>();
        int count = 1024;
        for (int i = 1; i <= count; i++) {
            map.put(i, String.valueOf(i));
            keyList.add(i);
            if (i % 2 == 0) {
                map.put(1 + i * count, String.valueOf(1 + i * count));
                keyList.add(1 + i * count);
            }
        }

        for (int i = 1; i <= count; i++) {
            Assert.assertTrue(map.containsKey(i));
            Assert.assertTrue(map.containsValue(String.valueOf(i)));
            if (i % 2 == 0) {
                Assert.assertTrue(map.containsKey(1 + i * count));
                Assert.assertTrue(map.containsValue(String.valueOf(1 + i * count)));
            }
        }

        for (int key : keyList) {
            Assert.assertEquals(String.valueOf(key), map.remove(key));
        }
    }

    @Test
    public void initializeBiggerMap() {
        int capacity = 1024;
        int entryCount = capacity * 10;
        AionMap<String, Integer> map = new AionMap<>(capacity, 6.5f);

        for (int i = 1; i <= entryCount; i++) {
            map.put("int_" + i, i);
        }
        Assert.assertEquals(entryCount, map.size());
        Assert.assertEquals(entryCount, map.values().size());
        Assert.assertEquals(entryCount, map.keySet().size());
        Assert.assertEquals(entryCount, map.entrySet().size());

        for (int i = 1; i <= entryCount; i++) {
            Assert.assertTrue(map.containsKey("int_" + i));
            Assert.assertTrue(map.containsValue(i));
        }

        for (int i = 1; i <= entryCount; i++) {
            Assert.assertEquals(i, (int) map.remove("int_" + i));
        }
    }

    @Test
    public void resizeTest() throws Exception {

        List<Integer> list = List.of(1, 2, 3, 9, 10);
        //resize when inserting 6th element
        float loadFactor = 0.75f;
        int initialCapacity = 8;

        AionMap<Integer, Integer> map = new AionMap<>(initialCapacity, loadFactor);

        Field entryTable = map.getClass().getDeclaredField("entryTable");
        entryTable.setAccessible(true);
        list.forEach(x -> {
            int index = x % initialCapacity;
            try {
                AionMap.AionMapEntry[] nodes = ((AionMap.AionMapEntry[]) entryTable.get(map));
                map.put(x, x);
                if (x <= list.size() / 2 + 1) {
                    Assert.assertEquals(x, nodes[index].getKey());
                    Assert.assertEquals(x, nodes[index].getValue());
                    Assert.assertNull(nodes[index].next);
                } else {
                    AionMap.AionMapEntry entry = nodes[index].next;
                    Assert.assertEquals(x, entry.getKey());
                    Assert.assertEquals(x, entry.getValue());
                    Assert.assertNull(entry.next);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        //trigger resize
        map.put(11, 11);

        AionMap.AionMapEntry[] nodes = ((AionMap.AionMapEntry[]) entryTable.get(map));
        Assert.assertEquals(initialCapacity * 2, nodes.length);
        list.forEach(x -> {
            Assert.assertEquals(x, nodes[x].getKey());
            Assert.assertEquals(x, nodes[x].getValue());
            Assert.assertNull(nodes[x].next);
        });
    }

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

    @Test
    public void checkEquals() {
        final int size = 20;
        final int hashCount = 10;
        AionMap<TestElement, String> map = createAionMap(size, hashCount);

        AionMap<TestElement, String> mapSame = map;
        Assert.assertTrue(map.equals(mapSame));

        AionMap<TestElement, String> mapDifferent = createAionMap(size, hashCount);
        Assert.assertFalse(map.equals(mapDifferent));
    }

    private AionMap<TestElement, String> createAionMap(int size, int hashCount) {
        AionMap<TestElement, String> map = new AionMap<>();
        for (int i = 0; i < size; ++i) {
            TestElement elt = new TestElement(i % hashCount, i);
            map.put(elt, elt.toString());
        }
        return map;
    }

    @Test
    public void checkHashCode() {
        final int size = 20;
        final int hashCount = 10;
        AionMap<TestElement, String> map = createAionMap(size, hashCount);

        AionMap<TestElement, String> mapSame = map;
        Assert.assertEquals(map.hashCode(), mapSame.hashCode());

        AionMap<TestElement, String> mapDifferent = createAionMap(size, hashCount);
        Assert.assertNotEquals(map.hashCode(), mapDifferent.hashCode());
    }

    @Test
    public void stressOperation() {
        AionMap<Integer, Integer> m = new AionMap<>();
        Integer res;

        m.clear();

        for (int i = 0; i < 10000; i++) {
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 0; i < 10000; i++) {
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 0; i < 10000; i++) {
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        m.clear();

        for (int i = 10000; i > 0; i--) {
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 10000; i > 0; i--) {
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 10000; i > 0; i--) {
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        m.clear();

        for (int i = 10000; i > 0; i--) {
            m.put(Integer.valueOf(i), Integer.valueOf(i));
        }

        for (int i = 10000; i > 0; i--) {
            res = m.get(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }

        for (int i = 1; i <= 10000; i++) {
            res = m.remove(Integer.valueOf(i));
            Assert.assertTrue(res.equals(Integer.valueOf(i)));
        }
    }

    private int createCollisionKey(int val) {
        return val * 16;
    }
}
