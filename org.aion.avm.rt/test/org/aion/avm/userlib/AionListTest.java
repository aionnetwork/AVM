package org.aion.avm.userlib;

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
}
