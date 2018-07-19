package org.aion.avm.userlib;

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
}
