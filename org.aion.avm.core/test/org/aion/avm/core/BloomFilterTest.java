package org.aion.avm.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.BloomFilter;
import org.junit.Test;

public class BloomFilterTest {
    private static final byte[] EMPTY = new byte[BloomFilter.SIZE];
    private static final byte[] FULL = new byte[BloomFilter.SIZE];

    static {
        for (int i = 0; i < BloomFilter.SIZE; i++) {
            FULL[i] = (byte) 0xff;
        }
    }

    @Test
    public void testOr() {
        BloomFilter filter1 = randomBloomFilter();
        BloomFilter filter2 = randomBloomFilter();

        System.out.println("Filter1: " + filter1);
        System.out.println("Filter2: " + filter2);

        BloomFilter orFilter = new BloomFilter(filter1.getBloomFilterBytes());
        orFilter.or(filter2);

        byte[] orBytes = getOrOfTwoByteArrays(filter1.getBloomFilterBytes(), filter2.getBloomFilterBytes());
        assertArrayEquals(orBytes, orFilter.getBloomFilterBytes());
    }

    @Test
    public void testOrOnEmptyFilters() {
        BloomFilter filter1 = emptyBloomFilter();
        BloomFilter filter2 = emptyBloomFilter();

        filter1.or(filter2);

        assertArrayEquals(EMPTY, filter1.getBloomFilterBytes());
    }

    @Test
    public void testOrOnFullFilters() {
        BloomFilter filter1 = fullBloomFilter();
        BloomFilter filter2 = fullBloomFilter();

        filter1.or(filter2);

        assertArrayEquals(FULL, filter1.getBloomFilterBytes());
    }

    @Test
    public void testAnd() {
        BloomFilter filter1 = randomBloomFilter();
        BloomFilter filter2 = randomBloomFilter();

        System.out.println("Filter1: " + filter1);
        System.out.println("Filter2: " + filter2);

        BloomFilter andFilter = new BloomFilter(filter1.getBloomFilterBytes());
        andFilter.and(filter2);

        byte[] andBytes = getAndOfTwoByteArrays(filter1.getBloomFilterBytes(), filter2.getBloomFilterBytes());
        assertArrayEquals(andBytes, andFilter.getBloomFilterBytes());
    }

    @Test
    public void testAndOnEmptyFilters() {
        BloomFilter filter1 = emptyBloomFilter();
        BloomFilter filter2 = emptyBloomFilter();

        filter1.and(filter2);

        assertArrayEquals(EMPTY, filter1.getBloomFilterBytes());
    }

    @Test
    public void testAndOnFullFilters() {
        BloomFilter filter1 = fullBloomFilter();
        BloomFilter filter2 = fullBloomFilter();

        filter1.and(filter2);

        assertArrayEquals(FULL, filter1.getBloomFilterBytes());
    }

    @Test
    public void testContains() {
        BloomFilter filter = randomBloomFilter();
        BloomFilter superset = produceSupersetBloomFilter(filter);

        System.out.println("Filter: " + filter);
        System.out.println("Superset: " + superset);

        // it is possible that superset equals filter.
        if (filter.equals(superset)) {
            assertTrue(filter.contains(superset));
        } else {
            assertFalse(filter.contains(superset));
        }

        assertTrue(superset.contains(filter));
    }

    @Test
    public void testContainsOnEmptyFilters() {
        BloomFilter filter1 = emptyBloomFilter();
        BloomFilter filter2 = emptyBloomFilter();

        assertTrue(filter1.contains(filter2));
        assertTrue(filter2.contains(filter1));
    }

    @Test
    public void testContainsOnFullFilters() {
        BloomFilter filter1 = fullBloomFilter();
        BloomFilter filter2 = fullBloomFilter();

        assertTrue(filter1.contains(filter2));
        assertTrue(filter2.contains(filter1));
    }

    private static byte[] getAndOfTwoByteArrays(byte[] array1, byte[] array2) {
        byte[] array = new byte[array1.length];
        for (int i = 0; i < array1.length; i++) {
            array[i] = (byte) (array1[i] & array2[i]);
        }
        return array;
    }

    private static byte[] getOrOfTwoByteArrays(byte[] array1, byte[] array2) {
        byte[] array = new byte[array1.length];
        for (int i = 0; i < array1.length; i++) {
            array[i] = (byte) (array1[i] | array2[i]);
        }
        return array;
    }

    private static BloomFilter emptyBloomFilter() {
        return new BloomFilter(EMPTY);
    }

    private static BloomFilter fullBloomFilter() {
        return new BloomFilter(FULL);
    }

    private static BloomFilter randomBloomFilter() {
        return new BloomFilter(Helpers.randomBytes(BloomFilter.SIZE));
    }

    /**
     * Returns a bloom filter such that every bit that is set in filter is also set in the returned
     * filter, and the returned filter may possibly have bits set that are not set in filter.
     */
    private static BloomFilter produceSupersetBloomFilter(BloomFilter filter) {
        BloomFilter superset = randomBloomFilter();
        superset.or(filter);
        return superset;
    }

}
