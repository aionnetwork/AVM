package org.aion.avm.core;

import java.util.Map;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Assert;
import org.aion.avm.internal.IHelper;


/**
 * Represents the long-lived global state of a specific "node" instance.
 * For now, this just contains the AvmSharedClassLoader (since it is stateless and shared by all transactions run on this
 * NodeEnvironment - that is, each AvmImpl instance).
 * Note that this is also responsible for any bootstrap initialization of the shared environment.  Specifically, this involves
 * eagerly loading the shadow JDK in order to run their <clinit> methods.
 */
public class NodeEnvironment {
    // NOTE:  This is only temporarily a singleton and will probably see its relationship inverted, in the future:  becoming the Avm factory.
    public static final NodeEnvironment singleton = new NodeEnvironment();

    private final AvmSharedClassLoader sharedClassLoader;

    private NodeEnvironment() {
        this.sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
        try {
            loadShadowClasses(NodeEnvironment.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // This would be a fatal startup error.
            Assert.unexpected(e);
        }
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


    private static void loadShadowClasses(ClassLoader loader) throws ClassNotFoundException {
        // Create the fake IHelper.
        IHelper.currentContractHelper.set(new IHelper() {
            @Override
            public void externalChargeEnergy(long cost) {
                Assert.unreachable("Shouldn't be called on this since we should be loading no instrumented classes");
            }
            @Override
            public void externalSetEnergy(long energy) {
                Assert.unreachable("Nobody should be calling this");
            }
            @Override
            public long externalGetEnergyRemaining() {
                Assert.unreachable("Nobody should be calling this");
                return 0L;
            }
            @Override
            public org.aion.avm.shadow.java.lang.Class<?> externalWrapAsClass(Class<?> input) {
                Assert.unreachable("Nobody should be calling this");
                return null;
            }
            @Override
            public org.aion.avm.shadow.java.lang.String externalWrapAsString(String input) {
                // This is called when creating enum constants, etc.
                return new org.aion.avm.shadow.java.lang.String(input);
            }
            @Override
            public int externalGetNextHashCode() {
                // We will just return 1 for all identity hash codes, for now.
                return 1;
            }
            @Override
            public void externalBootstrapOnly() {
                // This is ok since we are the bootstrapping helper.
            }});
        
        // Load all the classes - even just mentioning these might cause them to be loaded, even before the Class.forName().
        loadAndInitializeClasses(loader
                , org.aion.avm.shadow.java.lang.Boolean.class
                , org.aion.avm.shadow.java.lang.Byte.class
                , org.aion.avm.shadow.java.lang.Character.class
                , org.aion.avm.shadow.java.lang.Class.class
                , org.aion.avm.shadow.java.lang.Double.class
                , org.aion.avm.shadow.java.lang.Enum.class
                , org.aion.avm.shadow.java.lang.EnumConstantNotPresentException.class
                , org.aion.avm.shadow.java.lang.Exception.class
                , org.aion.avm.shadow.java.lang.Float.class
                , org.aion.avm.shadow.java.lang.Integer.class
                , org.aion.avm.shadow.java.lang.Long.class
                , org.aion.avm.shadow.java.lang.Math.class
                , org.aion.avm.shadow.java.lang.Number.class
                , org.aion.avm.shadow.java.lang.Object.class
                , org.aion.avm.shadow.java.lang.RuntimeException.class
                , org.aion.avm.shadow.java.lang.Short.class
                , org.aion.avm.shadow.java.lang.StrictMath.class
                , org.aion.avm.shadow.java.lang.String.class
                , org.aion.avm.shadow.java.lang.StringBuffer.class
                , org.aion.avm.shadow.java.lang.StringBuilder.class
                , org.aion.avm.shadow.java.lang.System.class
                , org.aion.avm.shadow.java.lang.Throwable.class
                , org.aion.avm.shadow.java.lang.TypeNotPresentException.class
                
                , org.aion.avm.shadow.java.lang.invoke.LambdaMetafactory.class
                , org.aion.avm.shadow.java.lang.invoke.StringConcatFactory.class
                
                , org.aion.avm.shadow.java.math.BigDecimal.class
                , org.aion.avm.shadow.java.math.BigInteger.class
                , org.aion.avm.shadow.java.math.MathContext.class
                , org.aion.avm.shadow.java.math.RoundingMode.class
        );
        
        // Clean-up.
        IHelper.currentContractHelper.set(null);
    }

    private static void loadAndInitializeClasses(ClassLoader loader, Class<?> ...classes) throws ClassNotFoundException {
        // (note that the loader.loadClass() doesn't invoke <clinit> so we use Class.forName() - this "initialize" flag should do that).
        boolean initialize = true;
        for (Class<?> clazz : classes) {
            Class<?> instance = Class.forName(clazz.getName(), initialize, loader);
            Assert.assertTrue(clazz == instance);
        }
    }
}
