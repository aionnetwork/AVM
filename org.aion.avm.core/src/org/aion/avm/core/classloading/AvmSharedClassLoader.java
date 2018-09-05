package org.aion.avm.core.classloading;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


/**
 * This classloader is meant to sit as parent to AvmClassLoader and only exists to handle the common code which we generate and treat
 * as part of the contract namespace, but is common and class-immutable across all contracts.
 */
public class AvmSharedClassLoader extends ClassLoader {
    // Bytecode Map of shared avm static classes
    private final Map<String, byte[]> bytecodeMap;

    // Class object cache
    // Note that AvmSharedClassLoader will also cache class objects from its parents to speed up class loading request
    private final Map<String, Class<?>> cache;

    // List of dynamic class generation handlers
    private ArrayList<Function<String, byte[]>> handlers;

    // If the initialization of the NodeEnvironment is done
    private boolean initialized = false;

    /**
     * Constructs a new AVM shared class loader.
     *
     * @param bytecodeMap the shared class bytecodes
     */
    public AvmSharedClassLoader(Map<String, byte[]> bytecodeMap) {
        this.bytecodeMap = bytecodeMap;
        this.cache = new ConcurrentHashMap<>();
        this.handlers = new ArrayList<>();

        registerHandlers();
    }

    // Register runtime class generator
    private void registerHandlers(){
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, this);
        this.handlers.add(wrapperGenerator);
    }

    public void finishInitialization() {
        for (String name: this.bytecodeMap.keySet()){
            try {
                this.loadClass(name, true);
            }catch (ClassNotFoundException e){
                RuntimeAssertionError.unreachable("Shared classloader initialization missing entry: " + name);
            }
        }
        this.initialized = true;
    }

    /**
     * Loads the class with the specified name.
     * This method will load two types of classes.
     * a) Statically generated shadow JCL classes
     * b) Dynamically generated shared classes (array wrappers)
     *
     * Other class loading requests will be delegated to its parent (By default, {@link ClassLoader})
     * Note that {@link AvmSharedClassLoader} will also cache the returned class object from its parent to speed up
     * concurrent class access.
     *
     * @param  name The name of the class
     *
     * @return  The resulting {@code Class} object
     *
     * @throws  ClassNotFoundException
     *          If the class was not found
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = null;
        boolean shouldResolve = resolve;

        // All user space class should be loaded with Dapp loader
        if (name.contains("org.aion.avm.user")){
            RuntimeAssertionError.unreachable("FAILED: Shared classloader receive request of: " + name);
        }

        // After initialization, all shadow JCL are loaded, we can get from cache without acquiring a lock
        // TODO: pre load static array wrappers
        if (initialized && name.contains("org.aion.avm.shadow.java") && !name.contains("org.aion.avm.arraywrapper")){
            // lock-free cache access
            result = this.cache.get(name);
            shouldResolve = false;
            RuntimeAssertionError.assertTrue(null != result);
        }else {
            // Acquire per class classloading lock
            synchronized (this.getClassLoadingLock(name)) {

                if (this.cache.containsKey(name)) {
                    result = this.cache.get(name);
                    // We got this from the cache so don't resolve.
                    shouldResolve = false;
                } else if (this.bytecodeMap.containsKey(name)) {
                    byte[] injected = this.bytecodeMap.get(name);
                    result = defineClass(name, injected, 0, injected.length);
                    this.cache.putIfAbsent(name, result);
                } else {
                    // Before falling back to the parent, try the dynamic.
                    for (Function<String, byte[]> handler : handlers) {
                        byte[] code = handler.apply(name);
                        if (code != null) {
                            result = defineClass(name, code, 0, code.length);
                            this.cache.putIfAbsent(name, result);
                            break;
                        }
                    }

                    if (null == result) {
                        result = getParent().loadClass(name);
                        // We got this from the parent so don't resolve.
                        shouldResolve = false;
                        this.cache.putIfAbsent(name, result);
                    }
                }
            }
        }

        if (null != result && shouldResolve) {
            resolveClass(result);
        }

        return result;
    }
}
