package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Just a helper to null out all static reference fields found in the given list of classes.
 * This only exists outside of LoadedDApp so it can be used in a unit test which demonstrates its behaviour.
 * NOTE:  The fieldCache provided MUST be populated.
 */
public class StaticClearer {
    public static void nullAllStaticFields(List<Class<?>> classes, Map<Class<?>, Field[]> fieldCache) {
        for (Class<?> clazz : classes) {
            for (Field field : fieldCache.get(clazz)) {
                // We only want static fields.
                if (Modifier.STATIC == (Modifier.STATIC & field.getModifiers())) {
                    // If this is a reference type, set it to null.
                    // (we will just use the java.lang.Object, since that is easier to test than shadow Object).
                    if (java.lang.Object.class.isAssignableFrom(field.getType())) {
                        try {
                            field.set(null, null);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            // We have interacted with this field, before, and setting the static to null should be ok.
                            RuntimeAssertionError.unexpected(e);
                        }
                    }
                }
            }
        }
    }
}
