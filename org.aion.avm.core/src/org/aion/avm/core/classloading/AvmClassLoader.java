package org.aion.avm.core.classloading;

import java.util.*;
import java.util.function.Function;


/**
 * NOTE:  This implementation assumes that the classes we are trying to load are "safe" in that they don't reference
 * anything we don't want this classloader to load.
 * While we originally imposed some of our isolation at the classloader level, we now assume that is done in the
 * bytecode instrumentation/analysis phase.
 */
public class AvmClassLoader extends ClassLoader {
    private Map<String, byte[]> classes;
    private List<Function<String, byte[]>> handlers;

    // Since we are using our own loadClass, we need our own cache.
    private final Map<String, Class<?>> cache;

    /**
     * Constructs a new AVM class loader.
     *
     * @param classes the transformed bytecode
     * @param handlers a list of handlers which can generate byte code for the given name.
     */
    public AvmClassLoader(Map<String, byte[]> classes, List<Function<String, byte[]>> handlers) {
        this.classes = classes;
        this.handlers = handlers;
        this.cache = new HashMap<>();
    }

    public AvmClassLoader(Map<String, byte[]> classes) {
        this(classes, Collections.emptyList());
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // NOTE:  We override this, instead of findClass, since we want to circumvent the normal delegation process of class loaders.
        Class<?> result = null;
        boolean shouldResolve = resolve;
        
        // We have a priority order to load:
        // 1) Cache
        // 2) Injected static code
        // 3) Dynamically generated
        // 4) Parent
        if (this.cache.containsKey(name)) {
            result = this.cache.get(name);
            // We got this from the cache so don't resolve.
            shouldResolve = false;
        } else if (this.classes.containsKey(name)) {
            byte[] injected = this.classes.get(name);
            result = defineClass(name, injected, 0, injected.length);
            this.cache.put(name, result);
        } else {
            // Before falling back to the parent, try the dynamic.
            for (Function<String, byte[]> handler : handlers) {
                byte[] code = handler.apply(name);
                if (code != null) {
                    result = defineClass(name, code, 0, code.length);
                    this.cache.put(name, result);
                    break;
                }
            }
            
            // If all else fails, the parent.
            if (null == result) {
                result = getParent().loadClass(name);
                // We got this from the parent so don't resolve.
                shouldResolve = false;
            }
        }
        
        if ((null != result) && shouldResolve) {
            resolveClass(result);
        }
        if (null == result) {
            throw new ClassNotFoundException();
        }
        return result;
    }
}