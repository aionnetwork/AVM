package org.aion.avm.core.classloading;

import java.util.*;
import java.util.function.Function;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.util.Assert;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * NOTE:  This implementation assumes that the classes we are trying to load are "safe" in that they don't reference
 * anything we don't want this classloader to load.
 * While we originally imposed some of our isolation at the classloader level, we now assume that is done in the
 * bytecode instrumentation/analysis phase.
 */
public class AvmClassLoader extends ClassLoader {
    // The ENUM modifier is defined in Class, but that is private so here is our copy of the constant.
    private static final int CLASS_IS_ENUM = 0x00004000;

    private Map<String, byte[]> classes;
    private ArrayList<Function<String, byte[]>> handlers;

    // Since we are using our own loadClass, we need our own cache.
    private final Map<String, Class<?>> cache;

    /**
     * Constructs a new AVM class loader.
     *
     * @param parent The explicitly required parent for the contract-namespace code which is shared across all contracts.
     * @param classes the transformed bytecode
     * @param handlers a list of handlers which can generate byte code for the given name.
     */
    public AvmClassLoader(AvmSharedClassLoader parent, Map<String, byte[]> classes, ArrayList<Function<String, byte[]>> handlers) {
        super(parent);
        this.classes = classes;
        this.handlers = handlers;
        this.cache = new HashMap<>();

        registerHandlers();
    }

    public AvmClassLoader(AvmSharedClassLoader parent, Map<String, byte[]> classes) {
        this(parent, classes, new ArrayList<>());
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
            // Note that this class loader should only be able to see classes we have transformed.  This means no enums.
            RuntimeAssertionError.assertTrue(0 == (CLASS_IS_ENUM & result.getModifiers()));
            this.cache.put(name, result);
        } else {

            // Before falling back to the parent, try the dynamic.
            for (Function<String, byte[]> handler : handlers) {
                if (name.contains("org.aion.avm.user")) {
                    byte[] code = handler.apply(name);
                    if (code != null) {
                        result = defineClass(name, code, 0, code.length);
                        this.cache.put(name, result);
                        break;
                    }
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

    /**
     * A helper for tests which want to load a class by its pre-renamed name and also ensure that the receiver was the loader (didn't delegate).
     * 
     * @param originalClassName The pre-renamed class name (.-style).
     * @return The transformed/renamed class instance.
     * @throws ClassNotFoundException Underlying load failed.
     */
    public Class<?> loadUserClassByOriginalName(String originalClassName) throws ClassNotFoundException {
        String renamedClass = PackageConstants.kUserDotPrefix + originalClassName;
        Class<?> clazz = this.loadClass(renamedClass);
        Assert.assertTrue(this == clazz.getClassLoader());
        return clazz;
    }

    //Internal
    public byte[] getUserClassBytecodeByOriginalName(String className){
        String renamedClass = PackageConstants.kUserDotPrefix + className;
        return this.classes.get(renamedClass);
    }

    public byte[] getUserClassBytecode(String className){
        return this.classes.get(className);
    }
}
