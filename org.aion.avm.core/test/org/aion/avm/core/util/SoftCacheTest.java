package org.aion.avm.core.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import org.junit.Assert;
import org.junit.Test;


public class SoftCacheTest {
    /**
     * This tests fills the cache with 64 KiB byte[] until it observes that the first one has been collected (via PhantomReference).
     * It then verifies that this entry is also missing from the cache.
     */
    @Test
    public void testFillUntilClear() throws Exception {
        SoftCache<String, byte[]> cache = new SoftCache<>();
        byte[] element1 = new byte[64 * 1024];
        String key1 = "element1";
        ReferenceQueue<byte[]> queue = new ReferenceQueue<>();
        PhantomReference<byte[]> watcher = new PhantomReference<>(element1, queue);
        cache.checkin(key1, element1);
        Assert.assertEquals(element1, cache.checkout(key1));
        Assert.assertNull(cache.checkout(key1));
        cache.checkin(key1, element1);
        int i = 0;
        while (watcher != queue.poll()) {
            String key = "key_" + i;
            byte[] value = new byte[64 * 1024];
            cache.checkin(key, value);
            i += 1;
        }
        Assert.assertNull(cache.checkout(key1));
    }
}
