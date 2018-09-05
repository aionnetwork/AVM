package org.aion.avm.userlib;

import java.util.Iterator;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Test;


public class AionListTest {
    /**
     * Creates an empty list, checks its size, and verifies we can't read or remove from it.
     */
    @Test
    public void emptyListTest() {
        AionList<Integer> list = new AionList<>();
        Assert.assertEquals(0, list.size());
        Assert.assertEquals(null, list.get(0));
        Assert.assertEquals(false, list.remove(Integer.valueOf(5)));
    }

    /**
     * Adds 100 elements, forcing growth, and then tries interacting with the list.
     */
    @Test
    public void addManyElements() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 100; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(100, list.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals("int_ " + i, list.get(i));
        }
        Assert.assertEquals(100, list.size());
        for (int i = 0; i < 100; ++i) {
            Assert.assertEquals(true, list.remove("int_ " + i));
        }
        Assert.assertEquals(0, list.size());
    }

    /**
     * Adds 50 elements, and verifies that only they are in the list.
     */
    @Test
    public void checkListContains() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 100; ++i) {
            if (0 == (i % 2)) {
                list.add("int_ " + i);
            }
        }
        Assert.assertEquals(50, list.size());
        for (int i = 0; i < 100; ++i) {
            boolean expected = (0 == (i % 2));
            boolean found = list.contains("int_ " + i);
            Assert.assertEquals(expected, found);
        }
    }

    /**
     * Adds 10 elements, then iterates them using iterator().
     */
    @Test
    public void checkIterator() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        Iterator<String> iterator = list.iterator();
        int counted = 0;
        while (iterator.hasNext()) {
            String elt = iterator.next();
            String expected = "int_" + counted;
            Assert.assertEquals(expected, elt);
            counted += 1;
        }
        Assert.assertEquals(10, counted);
    }

    /**
     * Adds 10 elements, then iterates them using iterator(), proving that we keep getting null once we reach the end.
     * TODO:  Update this to NoSuchElementException when issue-217 is done.
     */
    @Test
    public void checkIteratorDone() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        Iterator<String> iterator = list.iterator();
        int counted = 0;
        while (iterator.hasNext()) {
            String elt = iterator.next();
            String expected = "int_" + counted;
            Assert.assertEquals(expected, elt);
            counted += 1;
        }
        Assert.assertEquals(10, counted);
        
        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }

    /**
     * Adds 10 elements, then iterates them using listIterator().
     */
    @Test
    public void checkListIterator() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        ListIterator<String> iterator = list.listIterator();
        int counted = 0;
        while (iterator.hasNext()) {
            String elt = iterator.next();
            String expected = "int_" + counted;
            Assert.assertEquals(expected, elt);
            counted += 1;
        }
        Assert.assertEquals(10, counted);
    }

    /**
     * Adds 10 elements, then iterates them backward using listIterator(int).
     */
    @Test
    public void checkBackwardListIterator() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        ListIterator<String> iterator = list.listIterator(10);
        int index = 9;
        while (iterator.hasPrevious()) {
            String elt = iterator.previous();
            String expected = "int_" + index;
            Assert.assertEquals(expected, elt);
            index -= 1;
        }
        Assert.assertEquals(-1, index);
    }

    /**
     * Adds 10 elements, then iterates them backward using listIterator(int).
     */
    @Test (expected = IndexOutOfBoundsException.class)
    public void checkListIteratorOutOfBounds() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        list.listIterator(11);
    }

    /**
     * Adds 10 elements, then iterates them using the for loop shorthand.
     */
    @Test
    public void checkListLoopShorthand() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_" + i);
        }
        Assert.assertEquals(10, list.size());
        int counted = 0;
        for (String elt : list) {
            String expected = "int_" + counted;
            Assert.assertEquals(expected, elt);
            counted += 1;
        }
        Assert.assertEquals(10, counted);
    }
}
