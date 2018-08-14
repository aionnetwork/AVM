package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * A wrapper over the map that holds the cache of declared fields, per class.
 * Note that this cache is lazily populated by back-ending on the real reflection support.
 */
public class ReflectedFieldCache {
    private final Map<Class<?>, Field[]> fieldCache = new HashMap<>();

    public Field[] getDeclaredFieldsForClass(Class<?> clazz) {
        Field[] fields = this.fieldCache.get(clazz);
        if (null == fields) {
            fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // Note that this "setAccessible" will fail if the module is not properly "open".
                field.setAccessible(true);
            }
            this.fieldCache.put(clazz, fields);
        }
        return fields;
    }
}
