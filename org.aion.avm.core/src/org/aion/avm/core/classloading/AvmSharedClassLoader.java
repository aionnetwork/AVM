package org.aion.avm.core.classloading;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * This classloader is meant to sit as parent to AvmClassLoader and only exists to handle the common code which we generate and treat
 * as part of the contract namespace, but is common and class-immutable across all contracts.
 */
public class AvmSharedClassLoader extends ClassLoader {
    private Map<String, byte[]> classes;

    // Since we are using our own loadClass, we need our own cache.
    private final Map<String, Class<?>> cache;

    private ArrayList<Function<String, byte[]>> handlers;

    public AvmSharedClassLoader(Map<String, byte[]> classes) {
        this.classes = classes;
        this.cache = new HashMap<>();

        this.handlers = new ArrayList<>();
        registerHandlers();
    }

    private void registerHandlers(){
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, this);
        this.handlers.add(wrapperGenerator);
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
            // All user space class should be loaded with contract loader
            Assert.assertTrue(!name.contains("org.aion.avm.user"));

            // Before falling back to the parent, try the dynamic.
            for (Function<String, byte[]> handler : handlers) {
                byte[] code = handler.apply(name);
                if (code != null) {
                    result = defineClass(name, code, 0, code.length);

                    Assert.assertTrue(!this.cache.containsKey(name));
                    this.cache.put(name, result);
                    break;
                }
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
