package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    // Note that the constant map is a map of constant hashcodes to constant instances.  This is just provided so that reference deserialization
    // mechanisms can map from this primitive identity into the actual instances.
    private final Map<Integer, org.aion.avm.shadow.java.lang.Object> constantMap;

    private Class<?>[] shadowApiClasses;
    private Class<?>[] shadowClasses;
    private Class<?>[] arraywrapperClasses;
    private Class<?>[] exceptionwrapperClasses;
    private Set<String> jclClassNames;

    public final Map<String, Integer> shadowObjectSizeMap;  // pre-rename; shadow objects and exceptions
    public final Map<String, Integer> apiObjectSizeMap;     // post-rename; API objects
    public final Map<String, Integer> preRenameRuntimeObjectSizeMap;     // pre-rename; runtime objects including shadow objects, exceptions and API objects
    public final Map<String, Integer> postRenameRuntimeObjectSizeMap;    // post-rename; runtime objects including shadow objects, exceptions and API objects

    private NodeEnvironment() {
        Map<String, byte[]> generatedShadowJDK = CommonGenerators.generateShadowJDK();
        this.sharedClassLoader = new AvmSharedClassLoader(generatedShadowJDK);
        try {
            this.shadowApiClasses = new Class<?>[] {
                org.aion.avm.shadowapi.org.aion.avm.api.Address.class,
                org.aion.avm.shadowapi.org.aion.avm.api.BlockchainRuntime.class,
                org.aion.avm.shadowapi.org.aion.avm.api.Result.class,
            };

            this.arraywrapperClasses = new Class<?>[] {
                    org.aion.avm.arraywrapper.IArray.class
                    , org.aion.avm.arraywrapper.Array.class
                    , org.aion.avm.arraywrapper.ArrayElement.class
                    , org.aion.avm.arraywrapper.BooleanArray.class
                    , org.aion.avm.arraywrapper.ByteArray.class
                    , org.aion.avm.arraywrapper.CharArray.class
                    , org.aion.avm.arraywrapper.DoubleArray.class
                    , org.aion.avm.arraywrapper.FloatArray.class
                    , org.aion.avm.arraywrapper.IntArray.class
                    , org.aion.avm.arraywrapper.LongArray.class
                    , org.aion.avm.arraywrapper.ObjectArray.class
                    , org.aion.avm.arraywrapper.ShortArray.class
            };

            this.exceptionwrapperClasses = new Class<?>[] {
                org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable.class
            };

            this.shadowClasses = new Class<?>[] {
                    org.aion.avm.shadow.java.lang.AssertionError.class
                    , org.aion.avm.shadow.java.lang.Boolean.class
                    , org.aion.avm.shadow.java.lang.Byte.class
                    , org.aion.avm.shadow.java.lang.Character.class
                    , org.aion.avm.shadow.java.lang.CharSequence.class
                    , org.aion.avm.shadow.java.lang.Class.class
                    , org.aion.avm.shadow.java.lang.Comparable.class
                    , org.aion.avm.shadow.java.lang.Double.class
                    , org.aion.avm.shadow.java.lang.Enum.class
                    , org.aion.avm.shadow.java.lang.EnumConstantNotPresentException.class
                    , org.aion.avm.shadow.java.lang.Error.class
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

                    , org.aion.avm.shadow.java.util.Arrays.class
                    , org.aion.avm.shadow.java.util.Collection.class
                    , org.aion.avm.shadow.java.util.Iterator.class
                    , org.aion.avm.shadow.java.util.ListIterator.class
                    , org.aion.avm.shadow.java.util.Map.class
                    , org.aion.avm.shadow.java.util.Map.Entry.class
                    , org.aion.avm.shadow.java.util.NoSuchElementException.class
                    , org.aion.avm.shadow.java.util.Set.class
                    , org.aion.avm.shadow.java.util.List.class
                    , org.aion.avm.shadow.java.util.function.Function.class

                    , org.aion.avm.shadow.java.util.concurrent.TimeUnit.class
            };

            this.jclClassNames = new HashSet<>();

            // include the shadow classes we implement
            this.jclClassNames.addAll(loadShadowClasses(NodeEnvironment.class.getClassLoader(), shadowClasses));

            // we have to add the common generated exception/error classes as it's not pre-loaded
            this.jclClassNames.addAll(Stream.of(CommonGenerators.kExceptionClassNames)
                    .map(Helpers::fulllyQualifiedNameToInternalName)
                    .collect(Collectors.toList()));

            // include the invoke classes
            this.jclClassNames.add("java/lang/invoke/MethodHandles");
            this.jclClassNames.add("java/lang/invoke/MethodHandle");
            this.jclClassNames.add("java/lang/invoke/MethodType");
            this.jclClassNames.add("java/lang/invoke/CallSite");
            this.jclClassNames.add("java/lang/invoke/MethodHandles$Lookup");

            // Finish the initialization of shared class loader

            // Inject pre generated wrapper class into shared classloader enable more optimization opportunities for us
            this.sharedClassLoader.putIntoDynamicCache(this.arraywrapperClasses);

            // Inject shadow and api class into shared classloader so we can build a static cache
            this.sharedClassLoader.putIntoStaticCache(this.shadowClasses);
            this.sharedClassLoader.putIntoStaticCache(this.shadowApiClasses);
            this.sharedClassLoader.putIntoStaticCache(this.exceptionwrapperClasses);
            this.sharedClassLoader.finishInitialization();

        } catch (ClassNotFoundException e) {
            // This would be a fatal startup error.
            throw RuntimeAssertionError.unexpected(e);
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
            if (k.startsWith(PackageConstants.kShadowSlashPrefix)) {
                this.shadowObjectSizeMap.put(k.substring(PackageConstants.kShadowSlashPrefix.length()), v);
                this.postRenameRuntimeObjectSizeMap.put(k, v);
            }
            // the object size of API classes
            if (k.startsWith(PackageConstants.kShadowApiSlashPrefix)) {
                this.apiObjectSizeMap.put(k, v);
                this.preRenameRuntimeObjectSizeMap.put(k.substring(PackageConstants.kShadowApiSlashPrefix.length()), v);
            }
        });
        this.preRenameRuntimeObjectSizeMap.putAll(shadowObjectSizeMap);
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


    /**
     * Returns the API classes.
     *
     * @return a list of class objects
     */
    public List<Class<?>> getShadowApiClasses() {
        return Arrays.asList(shadowApiClasses);
    }

    /**
     * Returns the shadow classes. Note this does not include the exceptions.
     * @return
     */
    public List<Class<?>> getShadowClasses() {
        return Arrays.asList(shadowClasses);
    }

    /**
     * @return The map of constants (specified constant identity hash codes to constant instances).
     */
    public Map<Integer, org.aion.avm.shadow.java.lang.Object> getConstantMap() {
        return this.constantMap;
    }

    /**
     * Creates a new long-lived AVM instance.  The intention is that only one AVM instance will be created and reused for each transaction.
     * NOTE:  This is only in the NodeEnvironment since it is a long-lived singleton but this method has no strong connection to it so it
     * could be moved in the future.
     *
     * @param instrumentationFactory The factory to build IInstrumentation instances for the AVM's threads.
     * @param capabilities The external capabilities which this AVM instance can use.
     * @param configuration The configuration options for this new AVM instance.
     * @return The long-lived AVM instance.
     */
    public AvmImpl buildAvmInstance(IInstrumentationFactory instrumentationFactory, IExternalCapabilities capabilities, AvmConfiguration configuration) {
        AvmImpl avm = new AvmImpl(instrumentationFactory, capabilities, configuration);
        avm.start();
        return avm;
    }

    private static Set<String> loadShadowClasses(ClassLoader loader, Class<?>[] shadowClasses) throws ClassNotFoundException {
        // Create the fake IInstrumentation.
        IInstrumentation instrumentation = new IInstrumentation() {
            @Override
            public void chargeEnergy(long cost) throws OutOfEnergyException {
                // Shadow enum class will create array wrapper with <clinit>
                // Ignore the charge energy request in this case
            }
            @Override
            public long energyLeft() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public int getNextHashCodeAndIncrement() {
                // Only constants should end up being allocated under this so set them to the constant hash code we will over-write with their
                // specification values, after.
                return Integer.MIN_VALUE;
            }
            @Override
            public void bootstrapOnly() {
                // This is ok since we are the bootstrapping helper.
            }
            @Override
            public void setAbortState() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void clearAbortState() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public int getCurStackSize() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public int getCurStackDepth() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void enterMethod(int frameSize) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void exitMethod(int frameSize) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void enterCatchBlock(int depth, int size) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public int peekNextHashCode() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void forceNextHashCode(int nextHashCode) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void enterNewFrame(ClassLoader contractLoader, long energyLeft, int nextHashCode, IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>> classWrappers) {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public void exitCurrentFrame() {
                throw RuntimeAssertionError.unreachable("Nobody should be calling this");
            }
            @Override
            public boolean isLoadedByCurrentClassLoader(java.lang.Class userClass) {
                throw RuntimeAssertionError.unreachable("Not expected here.");
            }
        };

        // Load all the classes - even just mentioning these might cause them to be loaded, even before the Class.forName().
        InstrumentationHelpers.attachThread(instrumentation);
        Set<String> loadedClassNames = loadAndInitializeClasses(loader, shadowClasses);
        InstrumentationHelpers.detachThread(instrumentation);

        return loadedClassNames;
    }

    private static Set<String> loadAndInitializeClasses(ClassLoader loader, Class<?>... classes) throws ClassNotFoundException {
        Set<String> classNames = new HashSet<>();

        // (note that the loader.loadClass() doesn't invoke <clinit> so we use Class.forName() - this "initialize" flag should do that).
        boolean initialize = true;
        for (Class<?> clazz : classes) {
            Class<?> instance = Class.forName(clazz.getName(), initialize, loader);
            RuntimeAssertionError.assertTrue(clazz == instance);

            String className = Helpers.fulllyQualifiedNameToInternalName(clazz.getName());
            classNames.add(className.substring(PackageConstants.kShadowSlashPrefix.length()));
        }

        return classNames;
    }

    private Map<Integer, org.aion.avm.shadow.java.lang.Object> initializeConstantState() {
        Map<Integer, org.aion.avm.shadow.java.lang.Object> constantMap = new HashMap<>();

        // Note that these constants are defined, in the specification, to have these identity hash codes (all but RoundingMode override this but it matters for the persistence hash).
        // NOTE:  This list needs to be manually updated and we specify it as a list since these values CANNOT change, once assigned (these represent the serialized symbolic references from contracts).
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_TRUE, 1);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_FALSE, 2);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_ZERO, 3);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_ONE, 4);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigDecimal.avm_TEN, 5);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_UNLIMITED, 6);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL32, 7);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL64, 8);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.MathContext.avm_DECIMAL128, 9);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_UP, 10);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_DOWN, 11);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_CEILING, 12);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_FLOOR, 13);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_UP, 14);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_DOWN, 15);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN, 16);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.RoundingMode.avm_UNNECESSARY, 17);

        // Note that (as explained in issue-146), we need to treat our primitive "TYPE" pseudo-classes as constants, not like normal Class references.
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Boolean.avm_TYPE, 18);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Byte.avm_TYPE, 19);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Character.avm_TYPE, 20);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Double.avm_TYPE, 21);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Float.avm_TYPE, 22);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Integer.avm_TYPE, 23);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Long.avm_TYPE, 24);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.lang.Short.avm_TYPE, 25);

        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigInteger.avm_ZERO, 26);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigInteger.avm_ONE, 27);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigInteger.avm_TWO, 28);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.math.BigInteger.avm_TEN, 29);

        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_DAYS, 30);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_HOURS, 31);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_MINUTES, 32);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_SECONDS, 33);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_MILLISECONDS, 34);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_MICROSECONDS, 35);
        setConstantInstanceId(constantMap, org.aion.avm.shadow.java.util.concurrent.TimeUnit.avm_NANOSECONDS, 36);

        return constantMap;
    }

    private void setConstantInstanceId(Map<Integer, org.aion.avm.shadow.java.lang.Object> constantMap, org.aion.avm.shadow.java.lang.Object object, int identityHashCode) {
        object.persistenceToken = new ConstantPersistenceToken(identityHashCode);
        object.updateHashCodeForConstant(identityHashCode);
        constantMap.put(identityHashCode, object);
    }

    /**
     * Computes the object size of shadow java.base classes
     *
     * @return a mapping between class name and object size
     * <p>
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    protected Map<String, Integer> computeRuntimeObjectSizes(Map<String, byte[]> generatedShadowJDK) {
        // create a fake jar from API and shadow classes
        Map<String, byte[]> classBytesByQualifiedNames = new HashMap<>();
        String mainClassName = "java.lang.Object";

        List<Class<?>> classes = new ArrayList<>();
        classes.addAll(getShadowApiClasses());
        classes.addAll(getShadowClasses());
        for (Class<?> clazz : classes) {
            try {
                String name = clazz.getName();
                InputStream bytecode = clazz.getClassLoader().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
                classBytesByQualifiedNames.put(name, bytecode.readAllBytes());
            } catch (IOException e) {
                RuntimeAssertionError.unexpected(e);
            }
        }
        LoadedJar runtimeJar = new LoadedJar(classBytesByQualifiedNames, mainClassName);

        // get the forest and prune it to include only the "java.lang.Object" and "java.lang.Throwable" derived classes, as shown in the forest
        ClassHierarchyForest rtClassesForest = null;
        try {
            rtClassesForest = ClassHierarchyForest.createForestFrom(runtimeJar);
        } catch (IOException e) {
            // If the RT jar being something we can't process, our installation is clearly corrupt.
            throw RuntimeAssertionError.unexpected(e);
        }
        List<Forest.Node<String, ClassInfo>> newRoots = new ArrayList<>();
        newRoots.add(rtClassesForest.getNodeById("java.lang.Object"));
        newRoots.add(rtClassesForest.getNodeById("java.lang.Throwable"));
        rtClassesForest.prune(newRoots);

        // add the generated classes, i.e., exceptions in the generated shadow JDK
        for (String generatedClassName : generatedShadowJDK.keySet()) {
            // User cannot create the exception wrappers, so not to include them
            if (!generatedClassName.startsWith(PackageConstants.kExceptionWrapperDotPrefix)) {
                String parentName = CommonGenerators.parentClassMap.get(generatedClassName);
                byte[] parentClass;
                if (parentName == null) {
                    parentName = PackageConstants.kShadowDotPrefix + "java.lang.Throwable";
                    parentClass = rtClassesForest.getNodeById(parentName).getContent().getBytes();
                } else {
                    parentClass = generatedShadowJDK.get(parentName);
                }
                // TODO: figure out the name of the grandparent class
                rtClassesForest.add(new Forest.Node<>(parentName, new ClassInfo(false, parentClass)),
                        new Forest.Node<>(generatedClassName, new ClassInfo(false, generatedShadowJDK.get(generatedClassName))));
            }
        }

        // compute the object sizes in the pruned forest
        Map<String, Integer> rootObjectSizes = new HashMap<>();
        // "java.lang.Object" and "java.lang.Throwable" object sizes, measured with Instrumentation.getObjectSize() method (java.lang.Instrument).
        // A bare "java.lang.Object" has no fields and takes 16 bytes for 64-bit JDK. A "java.lang.Throwable" takes 40 bytes.
        rootObjectSizes.put("java/lang/Object", 16);
        rootObjectSizes.put("java/lang/Throwable", 40);
        return DAppCreator.computeUserObjectSizes(rtClassesForest, rootObjectSizes);
    }
}
