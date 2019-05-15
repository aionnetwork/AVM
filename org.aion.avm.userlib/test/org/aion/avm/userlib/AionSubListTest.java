package org.aion.avm.userlib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AionSubListTest {

    private static List<Integer> defaultAionList;
    private static int defaultListSize = 10;

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
        Assert.assertNull(subList.get(0));
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
        List<Integer> list = new ArrayList<>();
        list.add(100);
        list.add(200);

        defaultSubList.addAll(list);
        Assert.assertEquals(defaultListSize + 2, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex + 2, defaultSubList.size());
    }

    @Test
    public void testRemoveAll() {
        List<Integer> list = new ArrayList<>();
        list.add(defaultStartIndex + 1); //remove from sublist
        list.add(100); // this element is not in the sublist

        defaultSubList.removeAll(list);
        Assert.assertEquals(defaultListSize - 1, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex - 1, defaultSubList.size());
    }

    @Test
    public void testRetainAll() {
        List<Integer> list = new ArrayList<>();
        for (int i = defaultStartIndex; i < defaultEndIndex; i++) {
            list.add(i);
        }

        defaultSubList.retainAll(list);
        Assert.assertEquals(defaultListSize, defaultAionList.size());
        Assert.assertEquals(defaultEndIndex - defaultStartIndex, defaultSubList.size());
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

        printList(arrayList); printList(arraySubList); printList(aionList); printList(aionSubList); System.out.println(); // copy this line to see list values

        // remove a value and check
        int removeIndex = 3;
        arraySubList.remove(removeIndex);
        aionSubList.remove(removeIndex);

        printList(arrayList); printList(arraySubList); printList(aionList); printList(aionSubList); System.out.println(); // copy this line to see list values

        // retain values and check
        List<Integer> retainList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            retainList.add(i); //todo: can modify to see different behavior
        }

        aionSubList.retainAll(retainList);
        arraySubList.retainAll(retainList);
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

    private void printList(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                System.out.print("[");
            }
            if (i == list.size() -1) {
                System.out.println(list.get(i) + "]");
                break;
            }
            System.out.print(list.get(i) + ", ");
        }
    }

    private void assertAllEqual(Object... objects) {
        for (int i = 0; i < objects.length - 1; i++) {
            Assert.assertEquals(objects[i], objects[i + 1]);
        }
    }
}
