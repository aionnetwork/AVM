package org.aion.avm.core.shadowing;

import org.aion.avm.userlib.AionMap;


/**
 * Tests what happens when we call an unimplemented default method from within a DApp.
 */
public class TestDefaultMethodInClinitResource {
    static {
        AionMap<String, String> instance = new AionMap<>();
        // This is a default method so it should fail.
        instance.putIfAbsent("foo", "bar");
    }
}
