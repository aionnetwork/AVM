package org.aion.avm.userlib;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;


public class AionSetTest {
    /**
     * Creates an empty set, checks its size, and verifies we can't read or remove from it.
     */
    @Test
    public void emptySetTest() {
        AionSet<Integer> set = new AionSet<>();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(false, set.contains(Integer.valueOf(4)));
        Assert.assertEquals(false, set.remove(Integer.valueOf(5)));
    }

    /**
     * Adds 100 elements, forcing growth, and then tries interacting with the set.
     */
    @Test
    public void addManyElements() {
        AionSet<String> set = new AionSet<>();
        for (int i = 0; i < 100; ++i) {
            set.add("int_ " + i);
        }
        Assert.assertEquals(100, set.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals(true, set.contains("int_ " + i));
        }
        Assert.assertEquals(100, set.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals(true, set.remove("int_ " + i));
        }
        Assert.assertEquals(0, set.size());
    }

    /**
     * Adds the same 20 elements, over and over, verifying that nothing changes.
     */
    @Test
    public void addDuplicates() {
        AionSet<String> set = new AionSet<>();
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 20; ++j) {
                set.add("int_ " + j);
            }
        }
        Assert.assertEquals(20, set.size());
    }

    /**
     * Adds 10 elements, then tries to iterate them.
     */
    @Test
    public void checkIterator() {
        final int count = 10;
        boolean[] markMap = new boolean[count];
        AionSet<Integer> set = new AionSet<>();
        for (int i = 0; i < count; ++i) {
            set.add(i);
        }
        Assert.assertEquals(10, set.size());

        Iterator<Integer> iterator = set.iterator();
        int found = 0;
        while (iterator.hasNext()) {
            int elt = iterator.next();
            Assert.assertFalse(markMap[elt]);
            markMap[elt] = true;
            found += 1;
        }
        Assert.assertEquals(10, found);
    }

    /**
     * Adds 10 elements, then iterates them using iterator(), proving that we keep getting NoSuchElementException once we reach the end.
     */
    @Test
    public void checkIteratorDone() {
        final int count = 10;
        boolean[] markMap = new boolean[count];
        AionSet<Integer> set = new AionSet<>();
        for (int i = 0; i < count; ++i) {
            set.add(i);
        }
        Assert.assertEquals(10, set.size());

        Iterator<Integer> iterator = set.iterator();
        int found = 0;
        while (iterator.hasNext()) {
            int elt = iterator.next();
            Assert.assertFalse(markMap[elt]);
            markMap[elt] = true;
            found += 1;
        }
        Assert.assertEquals(10, found);
        
        Assert.assertFalse(iterator.hasNext());
        boolean didCatch = false;
        try {
            iterator.next();
            didCatch = false;
        } catch (NoSuchElementException e) {
            didCatch = true;
        }
        Assert.assertTrue(didCatch);
        Assert.assertFalse(iterator.hasNext());
        didCatch = false;
        try {
            iterator.next();
            didCatch = false;
        } catch (NoSuchElementException e) {
            didCatch = true;
        }
        Assert.assertTrue(didCatch);
    }

    /**
     * Adds 10 elements, then tries to iterate them using for loop short-hand.
     */
    @Test
    public void checkIterateForLoop() {
        final int count = 10;
        boolean[] markMap = new boolean[count];
        AionSet<Integer> set = new AionSet<>();
        for (int i = 0; i < count; ++i) {
            set.add(i);
        }
        Assert.assertEquals(10, set.size());

        int found = 0;
        for (int elt : set) {
            Assert.assertFalse(markMap[elt]);
            markMap[elt] = true;
            found += 1;
        }
        Assert.assertEquals(10, found);
    }

    /**
     * Adds 20 elements, using only 10 unique hashes, and then makes sure that all of the keys are still there.
     * This verifies that equals()-comparison is ultimately used to find duplicate keys, not depending on unique hashCode(), alone.
     */
    @Test
    public void addElementsWithCollidingHashes() {
        final int size = 20;
        final int hashCount = 10;
        AionSet<TestElement> set = new AionSet<>();
        for (int i = 0; i < size; ++i) {
            TestElement elt = new TestElement(i % hashCount, i);
            set.add(elt);
        }
        Assert.assertEquals(size, set.size());

        boolean[] markMap = new boolean[size];
        int[] hashes = new int[hashCount];
        int found = 0;
        for (TestElement elt : set) {
            Assert.assertFalse(markMap[elt.value]);
            markMap[elt.value] = true;
            hashes[elt.hash] += 1;
            found += 1;
        }
        Assert.assertEquals(size, found);
        for (int hash : hashes) {
            // We should see 2 of each.
            Assert.assertEquals(2, hash);
        }
    }
}
