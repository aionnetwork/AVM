package org.aion.avm.core.classloading;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;

import java.util.HashMap;
import java.util.Map;


/**
 * This classloader is meant to sit as parent to AvmClassLoader and only exists to handle the common code which we generate and treat
 * as part of the contract namespace, but is common and class-immutable across all contracts.
 */
public class AvmSharedClassLoader extends ClassLoader {
    private Map<String, byte[]> classes;

    // Since we are using our own loadClass, we need our own cache.
    private final Map<String, Class<?>> cache;

    public AvmSharedClassLoader(Map<String, byte[]> classes) {
        this.classes = classes;
        this.cache = new HashMap<>();
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // NOTE:  We override this, instead of findClass, since we want to circumvent the normal delegation process of class loaders.
        Class<?> result = null;
        boolean shouldResolve = resolve;
        
        if (this.cache.containsKey(name)) {
            result = this.cache.get(name);
            // We got this from the cache so don't resolve.
            shouldResolve = false;
        } else if (this.classes.containsKey(name)) {
            byte[] injected = this.classes.get(name);
            result = defineClass(name, injected, 0, injected.length);
            this.cache.put(name, result);
        } else {

            byte[] code = ArrayWrappingClassGenerator.arrayWrappingFactory(name, this);
            if (code != null) {
                result = defineClass(name, code, 0, code.length);
                this.cache.put(name, result);
            }

            if (null == result) {
                result = getParent().loadClass(name);
                // We got this from the parent so don't resolve.
                shouldResolve = false;
            }
        }
        
        if ((null != result) && shouldResolve) {
            resolveClass(result);
        }
        return result;
    }
}
