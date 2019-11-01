package org.aion.avm.userlib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class AionSubListTest {

    private static List<Integer> defaultAionList;
    private static int defaultListSize = 18;

    private static int defaultStartIndex = 3;
    private static int defaultEndIndex = 7;
    private static List<Integer> defaultSubList;

    @Before
    public void setup() {
        resetDefaultList();

        defaultSubList = defaultAionList.subList(defaultStartIndex, defaultEndIndex);
    }

    @Test
    public void emptySubListTest() {
        List<Integer> aionList = new AionList<>();
        List<Integer> subList = aionList.subList(0, 0);
        Assert.assertEquals(0, subList.size());
        boolean thrown = false;
        try {
            subList.get(0);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
        Assert.assertFalse(subList.remove(Integer.valueOf(10)));
    }


    @Test
    public void testSubListConstruction() {
        int rootListSize = 20;
        List<Integer> aionList = new AionList<>(); // from 0 - 19
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < rootListSize; i++) {
            aionList.add(i);
            arrayList.add(i);
        }

        // create first sublist
        int fromIndex1 = 0;
        int toIndex1 = 15; // from 0 - 14
        List<Integer> subList1 = aionList.subList(fromIndex1, toIndex1);
        List<Integer> arraySubList1 = arrayList.subList(fromIndex1, toIndex1);
        checkListsEqual(subList1, arraySubList1);

        // create sublist of sublist
        int fromIndex2 = 5;
        int toIndex2 = 10; //from 5 - 9
        List<Integer> subList2 = subList1.subList(fromIndex2, toIndex2);
        List<Integer> arraySubList2 = arrayList.subList(fromIndex2, toIndex2);
        checkListsEqual(subList2, arraySubList2);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(defaultEndIndex - defaultStartIndex, defaultSubList.size());
        Assert.assertFalse(defaultSubList.isEmpty());

        defaultSubList.clear();
        Assert.assertEquals(0, defaultSubList.size());
        Assert.assertTrue(defaultSubList.isEmpty());
    }

    @Test
    public void testContains() {
        int startingIndex = 3;
        int endingIndex = 7; // [3, 4, 5, 6]

        List<Integer> subList = defaultAionList.subList(startingIndex, endingIndex);

        for (int i = 0; i < defaultListSize; i++) {
            if (i >= startingIndex && i < endingIndex) {
                Assert.assertTrue(subList.contains(i));
            } else {
                Assert.assertFalse(subList.contains(i));
            }
        }
    }

    @Test
    public void testToArray() {
        int startingIndex = 3;
        int endingIndex = 7; // [3, 4, 5, 6]

        List<Integer> subList = defaultAionList.subList(startingIndex, endingIndex);
        Assert.assertArrayEquals(new Object[] {3, 4, 5, 6}, subList.toArray());

        subList.add(100);
        Assert.assertArrayEquals(new Object[] {3, 4, 5, 6, 100}, subList.toArray());

        subList.remove(2);
        Assert.assertArrayEquals(new Object[] {3, 4, 6, 100}, subList.toArray());
    }

    @Test
    public void testRemoveFirstIndex() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i + 1; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // remove element from start of sublist
                subList.remove(0);
                Assert.assertEquals(j - i - 1, subList.size()); // check that size decreased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test
    public void testRemoveLastIndex() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i + 1; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // remove element from end of sublist
                subList.remove(j - i - 1);
                Assert.assertEquals(j - i - 1, subList.size()); // check that size decreased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexTooLarge() {
        defaultSubList.remove(defaultEndIndex - defaultStartIndex); // -1 more should fail
    }

    @Test (expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexTooSmall() {
        defaultSubList.remove(-1);
    }

    @Test
    public void testRemoveFirstElement() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i + 1; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // remove element from start of sublist
                Assert.assertTrue(subList.remove(Integer.valueOf(i)));
                Assert.assertEquals(j - i - 1, subList.size()); // check that size decreased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test
    public void testRemoveLastElement() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i + 1; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // remove element from start of sublist
                Assert.assertTrue(subList.remove(Integer.valueOf(j - 1)));
                Assert.assertEquals(j - i - 1, subList.size()); // check that size decreased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test
    public void testRemoveElementNotInSublist() {
        Assert.assertFalse(defaultSubList.remove(Integer.valueOf(999)));
    }

    @Test
    public void testSublistAddElement() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // add element to sublist
                subList.add(100);
                Assert.assertEquals(j - i + 1, subList.size()); // check that size increased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test
    public void testSublistAddIndex() {
        for (int i = 0; i < defaultListSize; i++) {
            for (int j = i + 1; j < defaultListSize; j++) {
                List<Integer> subList = defaultAionList.subList(i, j);
                Assert.assertEquals(j - i, subList.size());

                // add element to sublist
                subList.add(j - i,100);
                Assert.assertEquals(j - i + 1, subList.size()); // check that size increased

                for (int k = 0; k < subList.size(); k++) {
                    Assert.assertEquals(defaultAionList.get(k + i), subList.get(k));
                }
                resetDefaultList();
            }
        }
    }

    @Test
    public void testContainsAll() {
        List<Integer> list = new ArrayList<>();
        for (int i = defaultStartIndex; i < defaultEndIndex; i++) {
            list.add(i);
        }

        Assert.assertTrue(defaultSubList.containsAll(list));

        list.add(999);
        Assert.assertFalse(defaultSubList.containsAll(list));
    }

    @Test
    public void testAddAll() {
        int size = defaultSubList.size();
        List<Integer> list = new ArrayList<>();
        list.add(100);
        list.add(200);

        defaultSubList.addAll(list);
        Assert.assertEquals(defaultListSize + 2, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex + 2, defaultSubList.size());
        Assert.assertEquals(100, (int) defaultSubList.get(size));
        Assert.assertEquals(200, (int) defaultSubList.get(size + 1));

        Assert.assertFalse(defaultSubList.addAll(new AionList<>()));

        defaultAionList.addAll(new AionList<>());
        boolean thrown = false;
        try {
            defaultSubList.addAll(list);
        } catch (Exception e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testRemoveAll() {
        List<Integer> list = new ArrayList<>();
        list.add(defaultStartIndex + 1); //remove from sublist
        list.add(100); // this element is not in the sublist

        Assert.assertTrue(defaultSubList.removeAll(list));
        Assert.assertEquals(defaultListSize - 1, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex - 1, defaultSubList.size());

        Assert.assertFalse(defaultSubList.removeAll(new AionList<Integer>()));
    }

    @Test
    public void testRetainAll() {
        List<Integer> list = new ArrayList<>();
        for (int i = defaultStartIndex; i < defaultEndIndex; i++) {
            list.add(i);
        }

        Assert.assertFalse(defaultSubList.retainAll(list));
        Assert.assertEquals(defaultListSize, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex, defaultSubList.size());

        list.remove(list.size() - 1);

        Assert.assertTrue(defaultSubList.retainAll(list));
        Assert.assertEquals(defaultListSize - 1, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex - 1, defaultSubList.size());

        Assert.assertTrue(defaultSubList.retainAll(new AionList<Integer>()));
    }

    /**
     * Create an ArrayList and an AionList, create a sublist from each, and verify the
     * required functionality match.
     */
    @Test
    public void testAionSubList() {
        int rootListsSize = 10;
        int startingIndex = 2;
        int endingIndex = 6;
        int subListSize = endingIndex - startingIndex;

        // initialize root lists
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> aionList = new AionList<>();

        for (int i = 0; i < rootListsSize; i++) {
            arrayList.add(i);
            aionList.add(i);
        }

        // initialize the sublist
        List<Integer> arraySubList = arrayList.subList(startingIndex, endingIndex);
        List<Integer> aionSubList = aionList.subList(startingIndex, endingIndex);

        // check initialization was successful
        for (int i = 0; i < subListSize; i ++) {
            assertAllEqual(arrayList.get(startingIndex + i), aionList.get(startingIndex + i), arraySubList.get(i), aionSubList.get(i));
        }

        checkListsEqual(arrayList, aionList);
        checkListsEqual(arraySubList, aionSubList);

        // add new value to sublists and check
        int newValue = 100;
        arraySubList.add(newValue);
        aionSubList.add(newValue);

        // remove a value and check
        int removeIndex = 3;
        arraySubList.remove(removeIndex);
        aionSubList.remove(removeIndex);

        // retain values and check
        List<Integer> retainList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            retainList.add(i); //todo: can modify to see different behavior
        }

        aionSubList.retainAll(retainList);
        arraySubList.retainAll(retainList);
    }

    @Test
    public void testClear(){
        int size = defaultSubList.size();
        List copyOfSublist = List.copyOf(defaultSubList);
        defaultSubList.clear();
        Assert.assertEquals(0, defaultSubList.size());
        Assert.assertEquals(defaultListSize - size, defaultAionList.size());
        for (Object o : copyOfSublist) {
            Assert.assertFalse(defaultAionList.contains(o));
            Assert.assertFalse(defaultSubList.contains(o));
        }
    }

    @Test
    public void testGet() {
        for (int i = 0; i < defaultSubList.size(); i++) {
            Assert.assertEquals(defaultStartIndex + i, (int) defaultSubList.get(i));
        }
        boolean thrown = false;
        try {
            defaultSubList.get(defaultEndIndex + 1);
        } catch (Exception e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testSet() {
        for (int i = 0; i < defaultSubList.size(); i++) {
            // increase all by 10
            int newItem = defaultStartIndex + i + 10;
            Assert.assertEquals(defaultStartIndex + i, (int) defaultSubList.set(i, newItem));
            Assert.assertEquals(newItem, (int) defaultAionList.get(defaultStartIndex + i));
            Assert.assertEquals(i, defaultSubList.indexOf(newItem));
            Assert.assertEquals(defaultStartIndex + i, defaultAionList.indexOf(newItem));
        }

        boolean thrown = false;
        try {
            defaultSubList.set(defaultEndIndex + 1, 10);
        } catch (Exception e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testAddIndex() {
        boolean thrown = false;
        try {
            defaultSubList.add(defaultEndIndex + 1, 10);
        } catch (Exception e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
        int sublistSize = defaultSubList.size();

        for (int i = 0; i < sublistSize; i++) {
            int newItem = defaultStartIndex + i + 10;
            defaultSubList.add(i, newItem);
            Assert.assertEquals(i, defaultSubList.indexOf(newItem));
            Assert.assertEquals(defaultStartIndex + i, defaultAionList.indexOf(newItem));
            Assert.assertEquals(newItem, (int) defaultAionList.get(defaultStartIndex + i));
            Assert.assertEquals(newItem, (int) defaultSubList.get(i));
            Assert.assertEquals(defaultListSize + i + 1, defaultAionList.size());
            Assert.assertEquals(sublistSize + i + 1, defaultSubList.size());
        }
    }

    @Test
    public void testRemove() {
        boolean thrown = false;
        try {
            defaultSubList.add(defaultEndIndex + 1, 10);
        } catch (Exception e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
        int sublistSize = defaultSubList.size();
        List copyOfSublist = List.copyOf(defaultSubList);

        for (int i = 0; i < sublistSize; i++) {
            defaultSubList.remove(0);
            Assert.assertEquals(defaultListSize - i - 1, defaultAionList.size());
            Assert.assertEquals(sublistSize - i - 1, defaultSubList.size());
            Assert.assertEquals(-1, defaultSubList.indexOf(copyOfSublist.get(i)));
            Assert.assertEquals(-1, defaultAionList.indexOf(copyOfSublist.get(i)));
            Assert.assertEquals(defaultStartIndex + i + 1, (int) defaultAionList.get(defaultStartIndex));
            if (i < sublistSize - 1) {
                Assert.assertEquals(defaultStartIndex + i + 1, (int) defaultSubList.get(0));
            }
        }
    }

    @Test
    public void testIndexOf(){
        Assert.assertEquals(-1, defaultSubList.indexOf(defaultStartIndex - 1));
        Assert.assertEquals(-1, defaultSubList.indexOf(defaultEndIndex + 1));
        for (int i = 0; i < defaultSubList.size(); i++) {
            Assert.assertEquals(i, defaultSubList.indexOf(defaultStartIndex + i));
        }
    }

    @Test
    public void testLastIndexOf(){
        int sublistSize = defaultSubList.size();
        defaultSubList.add(3);
        Assert.assertEquals(sublistSize, defaultSubList.lastIndexOf(3));
        Assert.assertEquals(defaultStartIndex + sublistSize, defaultAionList.lastIndexOf(3));
    }

    /**
     * sublist iterator tests
     **/
    @Test
    public void testIteratorRemove() {
        List<Integer> subListSizeOne = defaultAionList.subList(defaultStartIndex, defaultStartIndex + 1);
        Assert.assertEquals(1, subListSizeOne.size());

        Iterator itr = subListSizeOne.iterator();
        boolean thrown = false;
        try {
            itr.remove();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        itr.next();
        itr.remove();
        thrown = false;
        try {
            itr.remove();
        } catch (IllegalStateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testIteratorNext() {
        Iterator itr = defaultSubList.iterator();
        for (Integer i : defaultSubList) {
            Assert.assertTrue(itr.hasNext());
            Assert.assertEquals(i, itr.next());
        }
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testIteratorSet() {
        ListIterator<Integer> itr = defaultSubList.listIterator();
        for (int i = 0; i < defaultSubList.size(); i++) {
            // increase all by 10
            int newItem = defaultStartIndex + i + 10;
            itr.next();
            itr.set(newItem);
            Assert.assertEquals(newItem, (int) defaultAionList.get(defaultStartIndex + i));
            Assert.assertEquals(i, defaultSubList.indexOf(newItem));
            Assert.assertEquals(defaultStartIndex + i, defaultAionList.indexOf(newItem));
        }
    }

    @Test
    public void testIteratorAdd() {
        ListIterator<Integer> itr = defaultSubList.listIterator();
        int numberOfItemsToAdd = 5;
        int sublistSize = defaultSubList.size();

        for (int i = 0; i < numberOfItemsToAdd; i++) {
            int newItem = defaultStartIndex + i + 10;
            itr.add(newItem);
            Assert.assertEquals(i, defaultSubList.indexOf(newItem));
            Assert.assertEquals(defaultStartIndex + i, defaultAionList.indexOf(newItem));
            Assert.assertEquals(newItem, (int) defaultAionList.get(defaultStartIndex + i));
            Assert.assertEquals(newItem, (int) defaultSubList.get(i));
            Assert.assertEquals(defaultListSize + i + 1, defaultAionList.size());
            Assert.assertEquals(sublistSize + i + 1, defaultSubList.size());
        }
    }

    @Test
    public void listIteratorIndex() {
        int index = 1;
        ListIterator<Integer> itr = defaultSubList.listIterator(index);
        Assert.assertEquals(defaultSubList.get(index), itr.next());

        index = defaultSubList.size() - 1;
        itr = defaultSubList.listIterator(index);
        Assert.assertEquals(defaultSubList.get(index), itr.next());

        index = defaultSubList.size() + 1;
        boolean thrown = false;
        try {
            itr = defaultSubList.listIterator(index);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void sublistOfSublistConstruction(){
        List sublist1 = defaultSubList.subList(0, 1);
        Assert.assertEquals(1, sublist1.size());
        Assert.assertEquals(defaultSubList.get(0), sublist1.get(0));

        sublist1 = defaultSubList.subList(0, defaultSubList.size());
        Assert.assertEquals(defaultSubList.size(), sublist1.size());
        Assert.assertArrayEquals(defaultSubList.toArray(), sublist1.toArray());

        boolean thrown = false;
        try {
            defaultSubList.subList(0, defaultAionList.size() + 1);
        } catch (IndexOutOfBoundsException e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    /**
     * concurrent modification tests
     */

    @Test
    public void testConcurrentModificationAdd(){
        int sublistSize = defaultSubList.size();
        List<Integer> sublist1 = defaultSubList.subList(1, sublistSize -1); // 4 5
        List<Integer> sublist2 = defaultSubList.subList(2, sublistSize -1); // 5

        sublist2.add(10);

        // will throw a concurrent modification exception since it's not in the sublist hierarchy
        boolean thrown = false;
        try {
            sublist1.get(1);
        } catch (RuntimeException e){
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(10, (int) sublist2.get(1));
        Assert.assertEquals(10, (int) defaultSubList.get(sublistSize -1));
        Assert.assertEquals(10, (int) defaultAionList.get(defaultStartIndex + sublistSize -1));

        defaultSubList.add(20);
        thrown = false;
        try {
            sublist2.get(1);
        } catch (RuntimeException e){
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testConcurrentModificationRetainAll() {
        int sublistSize = defaultSubList.size();
        List<Integer> sublist1 = defaultSubList.subList(1, sublistSize - 1); // 4 5
        List<Integer> sublist2 = defaultSubList.subList(2, sublistSize - 1); // 5

        List item = new ArrayList<>(Arrays.asList(6, 7));
        sublist2.retainAll(item);

        // will throw a concurrent modification exception since it's not in the sublist hierarchy
        boolean thrown = false;
        try {
            sublist1.get(1);
        } catch (RuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(0, sublist2.toArray().length);
        Assert.assertEquals(sublistSize - 1, defaultSubList.toArray().length);
        Assert.assertEquals(defaultListSize - 1, defaultAionList.toArray().length);
    }

    @Test
    public void testConcurrentModificationClear() {
        int sublistSize = defaultSubList.size();
        List<Integer> sublist1 = defaultSubList.subList(1, sublistSize - 1); // 4 5
        List<Integer> sublist2 = defaultSubList.subList(2, sublistSize - 1); // 5

        sublist2.clear();

        // will throw a concurrent modification exception since it's not in the sublist hierarchy
        boolean thrown = false;
        try {
            sublist1.get(1);
        } catch (RuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(0, sublist2.toArray().length);
        Assert.assertEquals(sublistSize - 1, defaultSubList.toArray().length);
        Assert.assertEquals(defaultListSize - 1, defaultAionList.toArray().length);
    }

    @Test
    public void testConcurrentModificationIterator() {
        int sublistSize = defaultSubList.size();
        List<Integer> sublist1 = defaultSubList.subList(1, sublistSize - 1); // 4 5
        List<Integer> sublist2 = defaultSubList.subList(2, sublistSize - 1); // 5

        sublist2.clear();

        // will throw a concurrent modification exception since it's not in the sublist hierarchy
        boolean thrown = false;
        try {
            sublist1.iterator();
        } catch (RuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        sublist2.iterator();
        defaultSubList.iterator();
        defaultAionList.iterator();
    }

    @Test
    public void testIteratorConcurrentModification(){
        Iterator iterator = defaultSubList.iterator();
        defaultAionList.add(25);
        boolean thrown = false;
        try {
            iterator.next();
        } catch (RuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    private void resetDefaultList() {
        defaultAionList = new AionList<>();
        for (int i = 0; i < defaultListSize; i++) {
            defaultAionList.add(i);
        }
    }

    private void checkListsEqual(List<?> list1, List<?> list2) {
        Assert.assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            Assert.assertEquals(list1.get(i), list2.get(i));
        }
    }

    private void assertAllEqual(Object... objects) {
        for (int i = 0; i < objects.length - 1; i++) {
            Assert.assertEquals(objects[i], objects[i + 1]);
        }
    }
}
