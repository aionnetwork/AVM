package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.NoMainMethodException;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.internal.UncaughtException;
import org.aion.kernel.KernelInterface;


/**
 * Manages the organization of a DApp's root classes serialized shape as well as how to kick-off the serialization/deserialization
 * operations of the entire object graph (since both operations start at the root classes defined within the DApp).
 * Only the class statics and maybe a few specialized instances will be populated here.  The graph is limited by installing instance
 * stubs into fields pointing at objects.
 * 
 * We will store the data for all classes in a single storage key to avoid small IO operations when they are never used partially.
 * 
 * This class was originally just used to house the top-level calls related to serializing and deserializing a DApp but now it also
 * contains information relating to the DApp, in order to accomplish this.
 * Specifically, it now contains the ClassLoader, information about the class instances, and the cache of any reflection data.
 * NOTE:  It does NOT contain any information about the data currently stored within the Class objects associated with the DApp, nor
 * does it have any information about persisted aspects of the DApp (partly because it doesn't know anything about storage versioning).
 * 
 * NOTE:  Nothing here should be eagerly cached or looked up since the external caller is responsible for setting up the environment
 * such that it is fully usable.  Attempting to eagerly interact with it before then might not be safe.
 */
public class LoadedDApp {
    private final ClassLoader loader;
    private final byte[] address;
    private final List<Class<?>> classes;
    private final String originalMainClassName;
    // Note that this fieldCache is populated by the calls to ReflectionStructureCodec.
    private final ReflectedFieldCache fieldCache;

    // Other caches of specific pieces of data which are lazily built.
    private Class<?> helperClass;
    private Class<?> mainClass;
    private Constructor<?> helperConstructor;
    private Field helperBlockchainRuntimeField;
    private Method mainMethod;
    private Method helperClearTestingStateMethod;

    /**
     * Creates the LoadedDApp to represent the classes related to DApp at address.
     * 
     * @param loader The class loader to look up shape.
     * @param address The address of the contract.
     * @param classes The list of classes to populate (order must always be the same).
     */
    public LoadedDApp(ClassLoader loader, byte[] address, List<Class<?>> classes, String originalMainClassName) {
        this.loader = loader;
        this.address = address;
        this.classes = classes;
        this.originalMainClassName = originalMainClassName;
        this.fieldCache = new ReflectedFieldCache();
    }

    /**
     * Populates the statics of the DApp classes with the primitives and instance stubs described by the on-disk data.
     * 
     * @param feeProcessor The billing mechanism for storage operations.
     * @param kernelInterface The kernel storage API.
     */
    public void populateClassStaticsFromStorage(IStorageFeeProcessor feeProcessor, KernelInterface kernelInterface) {
        // We will create the field populator to build objects with the correct canonicalizing caches.
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(this.loader);
        // Create the codec which will make up the long-lived deserialization approach, within the system.
        ReflectionStructureCodec codec = new ReflectionStructureCodec(this.fieldCache, populator, feeProcessor, kernelInterface, this.address, 0);
        // The populator needs to know to attach the codec, itself, as the IDeserializer of new instances.
        populator.setDeserializer(codec);
        
        // Extract the raw data for the class statics.
        byte[] rawData = kernelInterface.getStorage(this.address, StorageKeys.CLASS_STATICS);
        feeProcessor.readStaticDataFromStorage(rawData.length);
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(rawData);
        
        // We will populate the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : this.classes) {
            codec.deserializeClass(decoder, clazz);
        }
    }

    /**
     * Used in the reentrant path to save out the statics held by the caller DApp instance, while replacing the statics it has with clones
     * pointing at instance stubs (which, themselves, are backed by the instances in the caller DApp).
     * Note that these can't be serialized since they point to the actual object graph we want to resume.
     * 
     * @param feeProcessor The billing mechanism for storage operations.
     * @return The graph processor which has captured the state of the statics.
     */
    public ReentrantGraphProcessor replaceClassStaticsWithClones(IStorageFeeProcessor feeProcessor) {
        ReentrantGraphProcessor processor = new ReentrantGraphProcessor(this.fieldCache, feeProcessor, this.classes);
        processor.captureAndReplaceStaticState();
        return processor;
    }

    /**
     * Serializes the static fields of the DApp classes and stores them on disk.
     * 
     * @param nextInstanceId The next instanceId to assign to an object which needs to be serialized.
     * @param feeProcessor The billing mechanism for storage operations.
     * @param kernelInterface The kernel storage API.
     */
    public long saveClassStaticsToStorage(long nextInstanceId, IStorageFeeProcessor feeProcessor, KernelInterface kernelInterface) {
        // Build the encoder.
        ReflectionStructureCodec codec = new ReflectionStructureCodec(this.fieldCache, null, feeProcessor, kernelInterface, this.address, nextInstanceId);
        StreamingPrimitiveCodec.Encoder encoder = StreamingPrimitiveCodec.buildEncoder();
        
        // Create the queue of instances reachable from here and consumer abstraction.
        Queue<org.aion.avm.shadow.java.lang.Object> instancesToWrite = new LinkedList<>();
        Consumer<org.aion.avm.shadow.java.lang.Object> instanceSink = new Consumer<>() {
            @Override
            public void accept(org.aion.avm.shadow.java.lang.Object t) {
                instancesToWrite.add(t);
            }};
        
        // We will serialize the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : this.classes) {
            codec.serializeClass(encoder, clazz, instanceSink);
        }
        
        // Save the raw bytes.
        byte[] rawData = encoder.toBytes();
        feeProcessor.writeStaticDataToStorage(rawData.length);
        kernelInterface.putStorage(this.address, StorageKeys.CLASS_STATICS, rawData);
        
        // Now, drain the queue.
        while (!instancesToWrite.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object instance = instancesToWrite.poll();
            codec.serializeInstance(instance, instanceSink);
        }
        
        // We need to pull out the nextInstanceId so that it can be set in the codec, when we are next invoked.
        return codec.getNextInstanceId();
    }

    /**
     * Loads and instantiates the IHelper instance to access the "Helper" statics within the given contractLoader.
     * This assumes that the bytecode for "Helper" is directly accessible within the DApp's classloader.
     * NOTE:  The current implementation is mostly cloned from Helpers.instantiateHelper() but we will inline/cache more of this,
     * over time, and that older implementation is only used by tests (which may be ported to use this).
     *
     * @param energyLimit The energy limit for this invocation.
     * @param nextHashCode The hashcode of the next object to be allocated (since this increments, across invocations).
     * @return The instance which will trampoline into the "Helper" statics called by the instrumented code within this contract.
     */
    public IHelper instantiateHelperInApp(long energyLimit, int nextHashCode) {
        IHelper helper = null;
        try {
            Constructor<?> helperConstructor = getHelperConstructor();
            helper = (IHelper) helperConstructor.newInstance(this.loader, energyLimit, nextHashCode);
        } catch (Throwable t) {
            // Errors at this point imply something wrong with the installation so fail.
            throw RuntimeAssertionError.unexpected(t);
        }
        return helper;
    }

    /**
     * Attaches a BlockchainRuntime instance to the Helper class (per contract) so DApp can
     * access blockchain related methods.
     * NOTE:  The current implementation is mostly cloned from Helpers.attachBlockchainRuntime() but we will inline/cache more of this,
     * over time, and that older implementation is only used by tests (which may be ported to use this).
     *
     * @param runtime The runtime to install in the DApp.
     */
    public void attachBlockchainRuntime(IBlockchainRuntime runtime) {
        try {
            getHelperBlochchainRuntimeField().set(null, runtime);
        } catch (Throwable t) {
            // Errors at this point imply something wrong with the installation so fail.
            throw RuntimeAssertionError.unexpected(t);
        }
    }

    /**
     * Calls the actual entry-point, running the whatever was setup in the attached blockchain runtime as a transaction and return the result.
     * 
     * @return The data returned from the transaction (might be null).
     * @throws OutOfEnergyError The transaction failed since the permitted energy was consumed.
     * @throws Exception Something unexpected went wrong with the invocation.
     */
    public byte[] callMain() throws Throwable {
        try {
            Method method = getMainMethod();
            ByteArray rawResult = (ByteArray) method.invoke(null);
            return (null != rawResult)
                    ? rawResult.getUnderlying()
                    : null;
        } catch (ClassNotFoundException | SecurityException | ExceptionInInitializerError e) {
            // should have been handled during CREATE.
            RuntimeAssertionError.unexpected(e);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new NoMainMethodException();

        } catch (InvocationTargetException e) {
            // handle the real exception
            handleUncaughtException(e.getCause());
        }

        return null;
    }

    /**
     * Forces all the classes defined within this DApp to be loaded and initialized (meaning each has its &lt;clinit&gt; called).
     * This is called during the create action to force the DApp initialization code to be run before it is stripped off for
     * long-term storage.
     */
    public void forceInitializeAllClasses() throws Throwable {
        for (Class<?> clazz : this.classes) {
            try {
                Class<?> initialized = Class.forName(clazz.getName(), true, this.loader);
                // These must be the same instances we started with and they must have been loaded by this loader.
                RuntimeAssertionError.assertTrue(clazz == initialized);
                RuntimeAssertionError.assertTrue(initialized.getClassLoader() == this.loader);
            } catch (ClassNotFoundException e) {
                // This error would mean that this is assembled completely incorrectly, which is a static error in our implementation.
                RuntimeAssertionError.unexpected(e);

            } catch (SecurityException e) {
                // This would mean that the shadowing is not working properly.
                RuntimeAssertionError.unexpected(e);

            } catch (ExceptionInInitializerError e) {
                // handle the real exception
                handleUncaughtException(e.getCause());
            }
        }
    }

    /**
     * The exception could be any {@link org.aion.avm.internal.AvmException}, any {@link java.lang.RuntimeException},
     * or a {@link org.aion.avm.exceptionwrapper.java.lang.Throwable}.
     */
    private void handleUncaughtException(Throwable cause) throws Throwable {
        // thrown by us
        if (cause instanceof AvmException) {
            throw cause;

            // thrown by runtime, but is never handled
        } else if (cause instanceof RuntimeException) {
            throw new UncaughtException(cause);

            // thrown by users
        } else if (cause instanceof org.aion.avm.exceptionwrapper.java.lang.Throwable) {
            throw new UncaughtException(cause.getCause());

        } else {
            RuntimeAssertionError.unexpected(cause);
        }
    }

    /**
     * Called before the DApp is about to be put into a cache.  This is so it can put itself into a "resumable" state.
     */
    public void cleanForCache() {
        // First, clear the state of the IHelper.
        try {
            Method helperClearTestingStateMethod = getHelperClearTestingStateMethod();
            helperClearTestingStateMethod.invoke(null);
        } catch (Throwable t) {
            // Errors at this point imply something wrong with the installation so fail.
            throw RuntimeAssertionError.unexpected(t);
        }
        
        // Second, null out all the instances referenced by the DApp's class statics (since we over-write them on load, anyway, this is a waste).
        // (note that we know the fieldCache must already contain this information since we were just asked to serialize, before this).
        StaticClearer.nullAllStaticFields(this.classes, this.fieldCache);
    }


    private Class<?> loadHelperClass() throws ClassNotFoundException {
        Class<?> helperClass = this.helperClass;
        if (null == helperClass) {
            String helperClassName = Helper.class.getName();
            helperClass = this.loader.loadClass(helperClassName);
            RuntimeAssertionError.assertTrue(helperClass.getClassLoader() == this.loader);
            this.helperClass = helperClass;
        }
        return helperClass;
    }

    private Class<?> loadMainClass() throws ClassNotFoundException {
        Class<?> mainClass = this.mainClass;
        if (null == mainClass) {
            String mappedUserMainClass = PackageConstants.kUserDotPrefix + this.originalMainClassName;
            mainClass = this.loader.loadClass(mappedUserMainClass);
            RuntimeAssertionError.assertTrue(mainClass.getClassLoader() == this.loader);
            this.mainClass = mainClass;
        }
        return mainClass;
    }

    private Constructor<?> getHelperConstructor() throws ClassNotFoundException, NoSuchMethodException {
        Constructor<?> helperConstructor = this.helperConstructor;
        if (null == helperConstructor) {
            Class<?> helperClass = loadHelperClass();
            helperConstructor = helperClass.getConstructor(ClassLoader.class, long.class, int.class);
            this.helperConstructor = helperConstructor;
        }
        return helperConstructor;
    }

    private Field getHelperBlochchainRuntimeField() throws ClassNotFoundException, NoSuchFieldException, SecurityException  {
        Field helperBlockchainRuntimeField = this.helperBlockchainRuntimeField;
        if (null == helperBlockchainRuntimeField) {
            Class<?> helperClass = loadHelperClass();
            helperBlockchainRuntimeField = helperClass.getField("blockchainRuntime");
            this.helperBlockchainRuntimeField = helperBlockchainRuntimeField;
        }
        return helperBlockchainRuntimeField;
    }

    private Method getMainMethod() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Method mainMethod = this.mainMethod;
        if (null == mainMethod) {
            Class<?> clazz = loadMainClass();
            mainMethod = clazz.getMethod("avm_main");
            this.mainMethod = mainMethod;
        }
        return mainMethod;
    }

    private Method getHelperClearTestingStateMethod() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Method helperClearTestingStateMethod = this.helperClearTestingStateMethod;
        if (null == helperClearTestingStateMethod) {
            Class<?> clazz = loadHelperClass();
            helperClearTestingStateMethod = clazz.getMethod("clearTestingState");
            this.helperClearTestingStateMethod = helperClearTestingStateMethod;
        }
        return helperClearTestingStateMethod;
    }

    /**
     * Dump the transformed class files of the loaded Dapp.
     * The output class files will be put under {@param path}.
     *
     * @param path The runtime to install in the DApp.
     */
    public void dumpTransformedByteCode(String path){
        AvmClassLoader appLoader = (AvmClassLoader) loader;
        for (Class<?> clazz : this.classes){
            byte[] bytecode = appLoader.getUserClassBytecode(clazz.getName());
            String output = path + "/" + clazz.getName() + ".class";
            Helpers.writeBytesToFile(bytecode, output);
        }
    }
}
