package org.aion.avm.internal;

import java.util.IdentityHashMap;


/**
 * Really just a high-level wrapper over an IdentityHashMap to contain real classes to shadow classes.
 * This exists because some of the logic was duplicated in a few places.
 */
public class InternedClasses {
    private final IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>> internedClassWrappers;

    public InternedClasses() {
        this.internedClassWrappers = new IdentityHashMap<>();
    }

    public org.aion.avm.shadow.java.lang.Class<?> get(Class<?> underlyingClass) {
        org.aion.avm.shadow.java.lang.Class<?> internedClass = this.internedClassWrappers.get(underlyingClass);
        if (null == internedClass) {
            internedClass = new org.aion.avm.shadow.java.lang.Class<>(underlyingClass);
            this.internedClassWrappers.put(underlyingClass, internedClass);
        }
        return internedClass;
    }
}
