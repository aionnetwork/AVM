package org.aion.avm.core;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.types.RawDappModule;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.PackageConstants;
import org.aion.kernel.KernelInterface;


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
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> constantMap;

    private Set<String> jclClassNames;

    public final Map<String, Integer> shadowObjectSizeMap;  // pre-rename; shadow objects and exceptions
    public final Map<String, Integer> apiObjectSizeMap;     // no rename needed; API objects
    public final Map<String, Integer> preRenameRuntimeObjectSizeMap;     // pre-rename; runtime objects including shadow objects, exceptions and API objects
    public final Map<String, Integer> postRenameRuntimeObjectSizeMap;    // post-rename; runtime objects including shadow objects, exceptions and API objects

    private NodeEnvironment() {
        Map<String, byte[]> generatedShadowJDK = CommonGenerators.generateShadowJDK();
        this.sharedClassLoader = new AvmSharedClassLoader(generatedShadowJDK);
        try {
            this.jclClassNames = loadShadowClasses(NodeEnvironment.class.getClassLoader());

            // we have to add the common generated exception/error classes as it's not pre-loaded
            this.jclClassNames.addAll(Stream.of(CommonGenerators.kExceptionClassNames)
                    .map(Helpers::fulllyQualifiedNameToInternalName)
                    .collect(Collectors.toList()));

            // TODO: confirm with Rom if this is correct
            this.jclClassNames.add("java/lang/invoke/MethodHandles");
            this.jclClassNames.add("java/lang/invoke/MethodHandles$Lookup");

        } catch (ClassNotFoundException e) {
            // This would be a fatal startup error.
            Assert.unexpected(e);
        }
        // Create the constant map.
        this.constantMap = Collections.unmodifiableMap(initializeConstantState());

        // create the object size look-up maps
        Map<String, Integer> rtObjectSizeMap = computeRuntimeObjectSizes(generatedShadowJDK);
        this.shadowObjectSizeMap = new HashMap<>();
        this.apiObjectSizeMap = new HashMap<>();
        this.preRenameRuntimeObjectSizeMap = new HashMap<>();
        this.postRenameRuntimeObjectSizeMap = new HashMap<>();
        rtObjectSizeMap.forEach((k, v) -> {
            // the shadowed object sizes; and change the class name to the non-shadowed version
            if(k.startsWith(PackageConstants.kShadowSlashPrefix)) {
                this.shadowObjectSizeMap.put(k.substring(PackageConstants.kShadowSlashPrefix.length()), v);
                this.postRenameRuntimeObjectSizeMap.put(k, v);
            }
            // the object size of API classes
            if(k.startsWith(PackageConstants.kApiSlashPrefix)) {
                this.apiObjectSizeMap.put(k, v);
            }
        });
        this.preRenameRuntimeObjectSizeMap.putAll(shadowObjectSizeMap);
        this.preRenameRuntimeObjectSizeMap.putAll(apiObjectSizeMap);
        this.postRenameRuntimeObjectSizeMap.putAll(apiObjectSizeMap);
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

    /**
     * Returns whether the class is from our custom JCL.
     *
     * @param classNameSlash
     * @return
     */
    public boolean isClassFromJCL(String classNameSlash) {
        return this.jclClassNames.contains(classNameSlash);
    }

    public Map<Long, org.aion.avm.shadow.java.lang.Object> getConstantMap() {
        return this.constantMap;
    }

    /**
     * Creates a new long-lived AVM instance.  The intention is that only one AVM instance will be created and reused for each transaction.
     * NOTE:  This is only in the NodeEnvironment since it is a long-lived singleton but this method has no strong connection to it so it
     * could be moved in the future.
     * 
     * @param kernel The kernel interface exposed by the consumer.
     * @return The long-lived AVM instance.
     */
    public Avm buildAvmInstance(KernelInterface kernel) {
        return new AvmImpl(kernel);
    }


    private static Set<String> loadShadowClasses(ClassLoader loader) throws ClassNotFoundException {
        // Create the fake IHelper.
        IHelper.currentContractHelper.set(new IHelper() {
            @Override
            public void externalChargeEnergy(long cost) {
                // Shadow enum class will create array wrapper with <clinit>
                // Ignore the charge energy request in this case
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
            public int externalGetNextHashCode() {
                // We will just return 1 for all identity hash codes, for now.
                return 1;
            }
            @Override
            public int captureSnapshotAndNextHashCode() {
                // We currently only use this for saving state prior to a reentrant call, which we don't expect during bootstrap.
                Assert.unreachable("Nobody should be calling this");
                return 0;
            }
            @Override
            public void applySpanshotAndNextHashCode(int nextHashCode) {
                // We currently only use this for restoring state after a reentrant call, which we don't expect during bootstrap.
                Assert.unreachable("Nobody should be calling this");
            }
            @Override
            public void externalBootstrapOnly() {
                // This is ok since we are the bootstrapping helper.
            }});
        
        // Load all the classes - even just mentioning these might cause them to be loaded, even before the Class.forName().
        Set<String> loadedClassNames = loadAndInitializeClasses(loader
                , org.aion.avm.shadow.java.lang.Boolean.class
                , org.aion.avm.shadow.java.lang.Byte.class
                , org.aion.avm.shadow.java.lang.Character.class
                , org.aion.avm.shadow.java.lang.CharSequence.class
                , org.aion.avm.shadow.java.lang.Class.class
                , org.aion.avm.shadow.java.lang.Double.class
                , org.aion.avm.shadow.java.lang.Enum.class
                , org.aion.avm.shadow.java.lang.EnumConstantNotPresentException.class
                , org.aion.avm.shadow.java.lang.Exception.class
                , org.aion.avm.shadow.java.lang.Float.class
                , org.aion.avm.shadow.java.lang.Integer.class
                , org.aion.avm.shadow.java.lang.Iterable.class
                , org.aion.avm.shadow.java.lang.Long.class
                , org.aion.avm.shadow.java.lang.Math.class
                , org.aion.avm.shadow.java.lang.Number.class
                , org.aion.avm.shadow.java.lang.Object.class
                , org.aion.avm.shadow.java.lang.Runnable.class
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

                , org.aion.avm.shadow.java.nio.Buffer.class
                , org.aion.avm.shadow.java.nio.ByteBuffer.class
                , org.aion.avm.shadow.java.nio.ByteOrder.class
                , org.aion.avm.shadow.java.nio.CharBuffer.class
                , org.aion.avm.shadow.java.nio.DoubleBuffer.class
                , org.aion.avm.shadow.java.nio.FloatBuffer.class
                , org.aion.avm.shadow.java.nio.IntBuffer.class
                , org.aion.avm.shadow.java.nio.LongBuffer.class
                , org.aion.avm.shadow.java.nio.ShortBuffer.class

                , org.aion.avm.shadow.java.util.Arrays.class
                , org.aion.avm.shadow.java.util.Collection.class
                , org.aion.avm.shadow.java.util.Iterator.class
                , org.aion.avm.shadow.java.util.ListIterator.class
                , org.aion.avm.shadow.java.util.Map.class
                , org.aion.avm.shadow.java.util.Map.Entry.class
                , org.aion.avm.shadow.java.util.Set.class
                , org.aion.avm.shadow.java.util.List.class
                , org.aion.avm.shadow.java.util.function.Function.class

        );
        
        // Clean-up.
        IHelper.currentContractHelper.remove();

        return loadedClassNames;
    }

    private static Set<String> loadAndInitializeClasses(ClassLoader loader, Class<?> ...classes) throws ClassNotFoundException {
        Set<String> classNames = new HashSet<>();

        // (note that the loader.loadClass() doesn't invoke <clinit> so we use Class.forName() - this "initialize" flag should do that).
        boolean initialize = true;
        for (Class<?> clazz : classes) {
            Class<?> instance = Class.forName(clazz.getName(), initialize, loader);
            Assert.assertTrue(clazz == instance);

            String className = Helpers.fulllyQualifiedNameToInternalName(clazz.getName());
            classNames.add(className.substring(PackageConstants.kShadowSlashPrefix.length()));
        }

        return classNames;
    }

    private Map<Long, org.aion.avm.shadow.java.lang.Object> initializeConstantState() {
        Map<Long, org.aion.avm.shadow.java.lang.Object> constantMap = new HashMap<>();
        
        // Assign the special "negative instanceId" values which we use for shadow JDK constants (public static objects and enum instances).
        // NOTE:  This list needs to be manually updated and we specify it as a list since these values CANNOT change, once assigned (these represent the serialized symbolic references from contracts).
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_TRUE, -1l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_FALSE, -2l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_ZERO, -3l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_ONE, -4l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_TEN, -5l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_UNLIMITED, -6l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL32, -7l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL64, -8l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL128, -89l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_UP, -10l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_DOWN, -11l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_CEILING, -12l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_FLOOR, -13l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_UP, -14l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_DOWN, -15l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN, -16l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_UNNECESSARY, -17l);
        
        // Note that (as explained in issue-146), we need to treat our primitive "TYPE" pseudo-classes as constants, not like normal Class references.
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_TYPE, -18l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Byte.avm_TYPE, -19l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Character.avm_TYPE, -20l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Double.avm_TYPE, -21l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Float.avm_TYPE, -22l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Integer.avm_TYPE, -23l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Long.avm_TYPE, -24l);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Short.avm_TYPE, -25l);
        
        return constantMap;
    }

    private void setConstantInstanceId(Map<Long, org.aion.avm.shadow.java.lang.Object> constantMap, org.aion.avm.shadow.java.lang.Object object, long instanceId) {
        object.instanceId = instanceId;
        constantMap.put(instanceId, object);
    }

    /**
     * Computes the object size of shadow java.base classes
     *
     * @return a mapping between class name and object size
     *
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    private Map<String, Integer> computeRuntimeObjectSizes(Map<String, byte[]> generatedShadowJDK) {
        RawDappModule runtimeModule;
        try {
            // build the runtime module from the jar TODO - this jar needs to be provided in a safe way
            String jarPath = System.getProperty("avm-rt-jar", "../out/jar/org-aion-avm-rt.jar");
            runtimeModule = RawDappModule.readFromJar(Helpers.readFileToBytes(jarPath));
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot find 'org-aion-avm-rt.jar'.");
        }

        // get the forest and prune it to include only the "java.lang.Object" and "java.lang.Throwable" derived classes, as shown in the forest
        ClassHierarchyForest rtClassesForest = runtimeModule.classHierarchyForest;
        List<Forest.Node<String, byte[]>> newRoots = new ArrayList<>();
        newRoots.add(rtClassesForest.getNodeById("java.lang.Object"));
        newRoots.add(rtClassesForest.getNodeById("java.lang.Throwable"));
        rtClassesForest.prune(newRoots);

        // add the generated classes, i.e., exceptions in the generated shadow JDK
        for (String generatedClassName : generatedShadowJDK.keySet()) {
            // User cannot create the exception wrappers, so not to include them
            if(!generatedClassName.startsWith("org.aion.avm.exceptionwrapper.")) {
                String parentName = CommonGenerators.parentClassMap.get(generatedClassName);
                byte[] parentClass;
                if (parentName == null) {
                    parentName = "org.aion.avm.shadow.java.lang.Throwable";
                    parentClass = rtClassesForest.getNodeById(parentName).getContent();
                }
                else {
                    parentClass = generatedShadowJDK.get(parentName);
                }
                rtClassesForest.add(new Forest.Node<>(parentName, parentClass),
                        new Forest.Node<>(generatedClassName, generatedShadowJDK.get(generatedClassName)));
            }
        }

        // compute the object sizes in the pruned forest
        Map<String, Integer> rootObjectSizes = new HashMap<>();
        // "java.lang.Object" and "java.lang.Throwable" object sizes, measured with Instrumentation.getObjectSize() method (java.lang.Instrument).
        // A bare "java.lang.Object" has no fields and takes 16 bytes for 64-bit JDK. A "java.lang.Throwable" takes 40 bytes.
        rootObjectSizes.put("java/lang/Object", 16);
        rootObjectSizes.put("java/lang/Throwable", 40);
        return  DAppCreator.computeUserObjectSizes(rtClassesForest, rootObjectSizes);
    }
}
