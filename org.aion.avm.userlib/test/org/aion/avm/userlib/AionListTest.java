package org.aion.avm.userlib;

import java.util.*;

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
     * Adds 10 elements, then iterates them using iterator(), proving that we keep getting NoSuchElementException once we reach the end.
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

    /**
     * Adds 10 elements, retain first 5 elements
     */
    @Test
    public void checkRetainFirstHalf() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(10, list.size());

        AionList<String> listToRetain = new AionList<>();
        for (int i = 0; i < 5; ++i) {
            listToRetain.add("int_ " + i);
        }

        list.retainAll(listToRetain);

        Assert.assertEquals(5, list.size());

        for (int i = 0; i < 5; ++i) {
            Assert.assertEquals("int_ " + i, list.get(i));
        }
    }

    /**
     * Adds 10 elements, retain latter 5 elements
     */
    @Test
    public void checkRetainSecondHalf() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(10, list.size());

        AionList<String> listToRetain = new AionList<>();
        for (int i = 5; i < 10; ++i) {
            listToRetain.add("int_ " + i);
        }

        list.retainAll(listToRetain);

        Assert.assertEquals(5, list.size());

        for (int i = 0; i < 5; ++i) {
            Assert.assertEquals("int_ " + (i + 5), list.get(i));
        }
    }

    /**
     * Select various elements to retain
     */
    @Test
    public void checkRetainAllWithSelection() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 100; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(100, list.size());

        int[] indexToRetain = { 5, 20, 70, 14, 35, 6};
        AionList<String> listToRetain = new AionList<>();

        for (int i: indexToRetain) {
            listToRetain.add("int_ " + i);
        }

        listToRetain.add("int_ " + 999); // this will not be retained, subtract one from expected length after retain all

        list.retainAll(listToRetain);

        Assert.assertEquals(listToRetain.size() - 1, list.size());
    }

    /**
     * Iterate front to back, and remove last element
     */
    @Test
    public void checkIteratorFrontToBackRemoveLastElement() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(10, list.size());

        Iterator<String> iterator = list.iterator();
        for (int i = 0; i < 10; ++i) {
            iterator.next();
        }

        iterator.remove();

        Assert.assertEquals(9, list.size());

        for (int i = 0; i < 9; ++i) {
            Assert.assertEquals("int_ " + i, list.get(i));
        }
    }

    /**
     * Iterate back to front, and remove first
     */
    @Test
    public void checkIteratorBackToFrontRemoveFirstElement() {
        AionList<String> list = new AionList<>();
        for (int i = 0; i < 10; ++i) {
            list.add("int_ " + i);
        }
        Assert.assertEquals(10, list.size());

        AionList<String>.AionListIterator iterator = (AionList<String>.AionListIterator) list.listIterator(10);

        for (int i = 0; i < 10; ++i) {
            iterator.previous();
        }

        iterator.remove();

        Assert.assertEquals(9, list.size());

        for (int i = 0; i < 9; ++i) {
            Assert.assertEquals("int_ " + (i + 1), list.get(i));
        }
    }

    @Test
    public void checkGetSubList() {
        AionList<Integer> aionList = new AionList<>();

        for (int i = 0; i < 10; i++) {
            aionList.add(i);
        }

        int fromIndex = 2;
        int toIndex = 5;
        List<Integer> aionSublist = aionList.subList(fromIndex, toIndex);

        Assert.assertEquals(toIndex - fromIndex, aionSublist.size());

        for (int i = 0; i < toIndex - fromIndex; i++) {
            Assert.assertEquals(aionList.get(i + fromIndex), aionSublist.get(i));
        }
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void checkCreateSublistWithInvalidStartingIndex() {
        AionList<Integer> aionList = new AionList<>();

        for (int i = 0; i < 10; i++) {
            aionList.add(i);
        }

        int fromIndex = -1;
        int toIndex = 5;
        aionList.subList(fromIndex, toIndex);
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void checkCreateSublistWithInvalidEndingIndex() {
        AionList<Integer> aionList = new AionList<>();

        for (int i = 0; i < 10; i++) {
            aionList.add(i);
        }

        int fromIndex = 2;
        int toIndex = 11;
        aionList.subList(fromIndex, toIndex);
    }
}
