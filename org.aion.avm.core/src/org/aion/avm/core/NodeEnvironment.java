package org.aion.avm.core;

import java.util.Map;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;


/**
 * Represents the long-lived global state of a specific "node" instance.
 * For now, this just contains the AvmSharedClassLoader (since it is stateless and shared by all transactions run on this
 * NodeEnvironment - that is, each AvmImpl instance).
 */
public class NodeEnvironment {
    // NOTE:  This is only temporarily a singleton and will probably see its relationship inverted, in the future:  becoming the Avm factory.
    public static final NodeEnvironment singleton = new NodeEnvironment();

    private final AvmSharedClassLoader sharedClassLoader;

    private NodeEnvironment() {
        this.sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
    }

    // This is an example of the more "factory-like" nature of the NodeEnvironment.
    public AvmClassLoader createInvocationClassLoader(Map<String, byte[]> finalContractClasses) {
        return new AvmClassLoader(this.sharedClassLoader, finalContractClasses);
    }

    public Class<?> loadSharedClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, this.sharedClassLoader);
    }

    /**
     * This method only exists for unit tests.  Returns true if clazz was loaded by the shared loader.
     */
    public boolean isClassFromSharedLoader(Class<?> clazz) {
        return (this.sharedClassLoader == clazz.getClassLoader());
    }
}
