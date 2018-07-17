package org.aion.avm.core;

import org.aion.avm.api.*;
import org.aion.avm.arraywrapper.*;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.instrument.HeapMemoryCostCalculator;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.persistence.AutomaticGraphVisitor;
import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.RootClassCodec;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.*;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApi;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransformedDappStorage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;


public class AvmImpl implements Avm {
    private static final String HELPER_CLASS = PackageConstants.kInternalSlashPrefix + "Helper";

    private static final TransformedDappStorage.CodeVersion VERSION = TransformedDappStorage.CodeVersion.VERSION_1_0;

    /**
     * Extracts the DApp module in compressed format into the designated folder.
     *
     * @param jar the DApp module in JAR format
     * @return the parsed DApp module if this operation is successful, otherwise null
     */
    static DappModule readDapp(byte[] jar) throws IOException {
        Objects.requireNonNull(jar);
        return DappModule.readFromJar(jar);
    }

    /**
     * Validates all classes, including but not limited to:
     *
     * <ul>
     * <li>class format (hash, version, etc.)</li>
     * <li>no native method</li>
     * <li>no invalid opcode</li>
     * <li>package name does not start with <code>org.aion.avm</code></li>
     * <li>no access to any <code>org.aion.avm</code> packages but the <code>org.aion.avm.api</code> package</li>
     * <li>any assumptions that the class transformation has made</li>
     * <li>TODO: add more</li>
     * </ul>
     *
     * @param dapp the classes of DApp
     * @return true if the DApp is valid, otherwise false
     */
    public boolean validateDapp(DappModule dapp) {

        // TODO: Rom, complete module validation

        return true;
    }

    /**
     * Computes the object size of shadow java.base classes
     *
     * @return a mapping between class name and object size
     *
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public static Map<String, Integer> computeShadowObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("java/lang/Object", 4);
        map.put("java/lang/Class", 4);
        map.put("java/lang/Math", 4);
        map.put("java/lang/String", 4);
        map.put("java/lang/Enum", 4);
        map.put("java/lang/StringBuffer", 4);
        map.put("java/lang/StringBuilder", 4);

        // TODO (issue-79):  Implement the rest of these by walking the runtime, elsewhere (this "4" is probably not right, in most cases).
        map.put("java/lang/AssertionError", 4);
        map.put("java/lang/Throwable", 4);
        map.put("java/lang/Exception", 4);
        map.put("java/lang/RuntimeException", 4);
        map.put("java/lang/NullPointerException", 4);
        map.put("java/lang/IllegalArgumentException", 4);
        map.put("java/math/MathContext", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Computes the object size of API classes.
     *
     * @return
     */
    public static Map<String, Integer> computeApiObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("org/aion/avm/api/Address", 4);
        map.put("org/aion/avm/api/InvalidTxDataException", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the sizes of all the classes, including the runtime ones and the DApp ones.
     *
     * @param classHierarchy     the class hierarchy
     * @param shadowObjectSizes  the object size of shadow java.base classes
     * @param apiObjectSizes     the object size of API classes
     * @return The size of user objects
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public static Map<String, Integer> computeUserObjectSizes(Forest<String, byte[]> classHierarchy,
                                                             Map<String, Integer> shadowObjectSizes,
                                                             Map<String, Integer> apiObjectSizes)
    {
        HeapMemoryCostCalculator objectSizeCalculator = new HeapMemoryCostCalculator();

        // copy over the runtime classes sizes
        HashMap<String, Integer> runtimeObjectSizes = new HashMap<>();
        runtimeObjectSizes.putAll(shadowObjectSizes);
        runtimeObjectSizes.putAll(apiObjectSizes);

        // compute the object size of every one in 'classes'
        objectSizeCalculator.calcClassesInstanceSize(classHierarchy, runtimeObjectSizes);


        Map<String, Integer> userObjectSizes = new HashMap<>();
        objectSizeCalculator.getClassHeapSizeMap().forEach((k, v) -> {
            if (!runtimeObjectSizes.containsKey(k)) {
                userObjectSizes.put(k, v);
            }
        });
        return userObjectSizes;
    }

    public static Map<String, Integer> computeAllObjectsSizes(Forest<String, byte[]> forest) {
        Map<String, Integer> map = new HashMap<>();
        map.putAll(computeShadowObjectSizes());
        map.putAll(computeApiObjectSizes());
        map.putAll(computeUserObjectSizes(forest, computeShadowObjectSizes(), computeApiObjectSizes()));

        return map;
    }

    public static Map<String, Integer> computeAllPostRenameObjectSizes(Forest<String, byte[]> forest) {
        Map<String, Integer> preRenameShadowObjectSizes = computeShadowObjectSizes();
        Map<String, Integer> preRenameApiObjectSizes = computeApiObjectSizes();
        Map<String, Integer> preRenameUserObjectSizes = computeUserObjectSizes(forest, preRenameShadowObjectSizes, preRenameApiObjectSizes);

        Map<String, Integer> postRenameObjectSizes = new HashMap<>(preRenameApiObjectSizes);
        for (Map.Entry<String, Integer> entry : preRenameUserObjectSizes.entrySet()) {
            postRenameObjectSizes.put(PackageConstants.kUserSlashPrefix + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : preRenameShadowObjectSizes.entrySet()) {
            postRenameObjectSizes.put(PackageConstants.kShadowSlashPrefix + entry.getKey(), entry.getValue());
        }
        return postRenameObjectSizes;
    }

    /**
     * Replaces the <code>java.base</code> package with the shadow implementation.
     *
     * @param classes        the class of DApp (names specified in .-style)
     * @param preRenameClassHierarchy The pre-rename hierarchy of user-defined classes in the DApp (/-style).
     * @return the transformed classes and any generated classes (names specified in .-style)
     */
    public Map<String, byte[]> transformClasses(Map<String, byte[]> classes, Forest<String, byte[]> preRenameClassHierarchy) {

        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        Map<String, byte[]> processedClasses = new HashMap<>();
        // WARNING:  This dynamicHierarchyBuilder is both mutable and shared by TypeAwareClassWriter instances.
        HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        // We also want to expose this type to the class writer so it can compute common superclasses.
        ExceptionWrapping.GeneratedClassConsumer generatedClassesSink = (superClassSlashName, classSlashName, bytecode) -> {
            // Note that the processed classes are expected to use .-style names.
            String classDotName = Helpers.internalNameToFulllyQualifiedName(classSlashName);
            processedClasses.put(classDotName, bytecode);
            String superClassDotName = Helpers.internalNameToFulllyQualifiedName(superClassSlashName);
            dynamicHierarchyBuilder.addClass(classDotName, superClassDotName, bytecode);
        };
        Set<String> preRenameUserDefinedClasses = ClassWhiteList.extractDeclaredClasses(preRenameClassHierarchy);
        Set<String> preRenameUserClassSet = classes.keySet();
        ParentPointers parentClassResolver = new ParentPointers(preRenameUserDefinedClasses, preRenameClassHierarchy);
        Map<String, Integer> postRenameObjectSizes = computeAllPostRenameObjectSizes(preRenameClassHierarchy);

        for (String name : classes.keySet()) {
            // Note that transformClasses requires that the input class names by the .-style names.
            Assert.assertTrue(-1 == name.indexOf("/"));

            // We need to parse with EXPAND_FRAMES, since the StackWatcherClassAdapter uses a MethodNode to parse methods.
            // We also add SKIP_DEBUG since we aren't using debug data and skipping it removes extraneous labels which would otherwise
            // cause the BlockBuildingMethodVisitor to build lots of small blocks instead of a few big ones (each block incurs a Helper
            // static call, which is somewhat expensive - this is how we bill for energy).
            int parsingOptions = ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG;
            byte[] bytecode = new ClassToolchain.Builder(classes.get(name), parsingOptions)
                    .addNextVisitor(new RejectionClassVisitor())
                    .addNextVisitor(new UserClassMappingVisitor(preRenameUserClassSet))
                    .addNextVisitor(new ConstantVisitor(HELPER_CLASS))
                    .addNextVisitor(new ClassMetering(HELPER_CLASS, postRenameObjectSizes))
                    .addNextVisitor(new InvokedynamicShadower(PackageConstants.kShadowSlashPrefix))
                    .addNextVisitor(new ClassShadowing(HELPER_CLASS))
                    .addNextVisitor(new StackWatcherClassAdapter())
                    .addNextVisitor(new ExceptionWrapping(HELPER_CLASS, parentClassResolver, generatedClassesSink))
                    .addNextVisitor(new AutomaticGraphVisitor())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentClassResolver, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            bytecode = new ClassToolchain.Builder(bytecode, parsingOptions)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentClassResolver, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            String mappedName = PackageConstants.kUserDotPrefix + name;
            processedClasses.put(mappedName, bytecode);
        }
        return processedClasses;
    }

    public static class BlockchainRuntimeImpl extends org.aion.avm.shadow.java.lang.Object implements IBlockchainRuntime {

        private Transaction tx;
        private Block block;
        private KernelApi cb;
        private IHelper helper;

        public BlockchainRuntimeImpl(Transaction tx, Block block, KernelApi cb, IHelper helper) {
            this.tx = tx;
            this.block = block;
            this.cb = cb;
            this.helper = helper;
        }

        @Override
        public Address avm_getSender() {
            return new Address(tx.getFrom());
        }

        @Override
        public Address avm_getAddress() {
            // TODO: handle CREATE transaction
            return new Address(tx.getTo());
        }

        @Override
        public long avm_getEnergyLimit() {
            return tx.getEnergyLimit();
        }

        @Override
        public ByteArray avm_getData() {
            return new ByteArray(tx.getData());
        }

        @Override
        public ByteArray avm_getStorage(ByteArray key) {
            return new ByteArray(cb.getStorage(tx.getTo(), key.getUnderlying()));
        }

        @Override
        public void avm_putStorage(ByteArray key, ByteArray value) {
            cb.putStorage(tx.getTo(), key.getUnderlying(), value.getUnderlying());
        }

        @Override
        public void avm_updateCode(ByteArray newCode) {
            cb.updateCode(tx.getTo(), newCode.getUnderlying());
        }

        @Override
        public void avm_selfDestruct(Address beneficiary) {
            cb.selfdestruct(tx.getTo(), beneficiary.unwrap());
        }

        @Override
        public long avm_getBlockEpochSeconds() {
            return block.getTimestamp();
        }

        @Override
        public long avm_getBlockNumber() {
            return block.getNumber();
        }

        @Override
        public ByteArray avm_sha3(ByteArray data) {
            // TODO: we can implement this inside vm
            return null;
        }

        @Override
        public ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
            AvmResult result = cb.call(tx.getTo(), targetAddress.unwrap(), value, data.getUnderlying(), energyLimit);

            // Reset the thread-local helper instance
            IHelper.currentContractHelper.set(helper);

            // charge energy consumed
            long energyUsed = energyLimit - result.energyLeft;
            helper.externalChargeEnergy(energyUsed);

            return new ByteArray(result.returnData);
        }

        @Override
        public void avm_log(ByteArray index0, ByteArray data) {
            cb.log(tx.getTo(), index0.getUnderlying(), data.getUnderlying());
        }
    }

    @Override
    public AvmResult run(Transaction tx, Block block, KernelApi cb) {
        switch (tx.getType()) {
            case CREATE:
                return create(tx, block, cb);
            case CALL:
                return call(tx, block, cb);
            default:
                return new AvmResult(AvmResult.Code.INVALID_TX, 0);
        }
    }

    public AvmResult create(Transaction tx, Block block, KernelApi cb) {
        try {
            // read dapp module
            byte[] dappAddress = tx.getTo(); // TODO: The contract address should be computed based on consensus rules
            byte[] dappCode = tx.getData();
            DappModule app = readDapp(dappCode);
            if (app == null) {
                return new AvmResult(AvmResult.Code.INVALID_JAR, 0);
            }

            // validate dapp module
            if (!validateDapp(app)) {
                return new AvmResult(AvmResult.Code.INVALID_CODE, 0);
            }
            ClassHierarchyForest dappClassesForest = app.getClassHierarchyForest();

            // transform
            Map<String, byte[]> transformedClasses = transformClasses(app.getClasses(), dappClassesForest);
            app.setClasses(transformedClasses);

            // As per usual, we need to get the special Helper class for each contract loader.
            Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(transformedClasses);

            // Construct the per-contract class loader and access the per-contract IHelper instance.
            AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);

            // We start the nextHashCode at 1.
            int nextHashCode = 1;
            IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit(), nextHashCode);
            Helpers.attachBlockchainRuntime(classLoader, new BlockchainRuntimeImpl(tx, block, cb, helper));

            // billing the Processing cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                    + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * app.bytecodeSize * (1 + app.numberOfClasses) / 10);

            // store transformed dapp
            byte[] transformedDappJar = app.createJar(dappAddress);
            cb.putTransformedCode(dappAddress, VERSION, transformedDappJar);

            // billing the Storage cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * tx.getData().length);

            // TODO: create invocation need further discussion

            String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
            Class<?> clazz = classLoader.loadClass(mappedUserMainClass);

            Method method = clazz.getMethod("avm_init");
            method.invoke(null);

            // Save back the state before we return - we haven't saved anything so the instance count starts at 1.
            long nextInstanceId = 1l;
            // -first, save out the classes
            // TODO: Make this fully walk the graph
            // TODO: Get the updated "nextInstanceId" after everything is written to storage.
            RootClassCodec.saveClassStaticsToStorage(classLoader, nextInstanceId, cb, dappAddress, getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet()));
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(cb, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId));

            // TODO: whether we should return the dapp address is subject to change
            return new AvmResult(AvmResult.Code.SUCCESS, helper.externalGetEnergyRemaining(), dappAddress);
        } catch (FatalAvmError e) {
            // These are unrecoverable errors (either a bug in our code or a lower-level error reported by the JVM).
            // (for now, we System.exit(-1), since this is what ethereumj does, but we may want a more graceful shutdown in the future)
            e.printStackTrace();
            System.exit(-1);
            return null;
        } catch (OutOfEnergyError e) {
            return new AvmResult(AvmResult.Code.OUT_OF_ENERGY, 0);
        } catch (AvmException e) {
            // We handle the generic AvmException as some failure within the contract.
            return new AvmResult(AvmResult.Code.FAILURE, 0);
        } catch (Throwable t) {
            // There should be no other reachable kind of exception.  If we reached this point, something very strange is happening so log
            // this and bring us down.
            t.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public AvmResult call(Transaction tx, Block block, KernelApi cb) {
        // retrieve the transformed bytecode
        byte[] dappAddress = tx.getTo();
        DappModule app;
        try {
            byte[] transformedDappJar = cb.getTransformedCode(dappAddress);
            app = DappModule.readFromJar(transformedDappJar);
        } catch (IOException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
        }

        // As per usual, we need to get the special Helper class for each contract loader.
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);

        // Construct the per-contract class loader and access the per-contract IHelper instance.
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        
        // Load all the user-defined classes (these are required for both loading and storing state).
        List<Class<?>> aphabeticalContractClasses = getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet());

        // Load the initial state of the environment.
        ContractEnvironmentState initialState = ContractEnvironmentState.loadFromStorage(cb, dappAddress);
        
        // TODO:  We might be able to move this setup of IHelper to later in the load once we get rid of the <clinit> (requires energy).
        IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit(), initialState.nextHashCode);
        Helpers.attachBlockchainRuntime(classLoader, new BlockchainRuntimeImpl(tx, block, cb, helper));

        // Now that we can load classes for the contract, load and populate all their classes.
        RootClassCodec.populateClassStaticsFromStorage(classLoader, cb, dappAddress, aphabeticalContractClasses);

        // load class
        try {
            String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
            Class<?> clazz = classLoader.loadClass(mappedUserMainClass);

            Method method = clazz.getMethod("avm_main");
            byte[] ret = ((ByteArray) method.invoke(null)).getUnderlying();

            // Save back the state before we return.
            // -first, save out the classes
            // TODO: Make this fully walk the graph
            // TODO: Get the updated "nextInstanceId" after everything is written to storage.
            RootClassCodec.saveClassStaticsToStorage(classLoader, initialState.nextInstanceId, cb, dappAddress, aphabeticalContractClasses);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(cb, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), initialState.nextInstanceId));

            return new AvmResult(AvmResult.Code.SUCCESS, helper.externalGetEnergyRemaining(), ret);
        } catch (OutOfEnergyError e) {
            return new AvmResult(AvmResult.Code.OUT_OF_ENERGY, 0);
        } catch (Exception e) {
            e.printStackTrace();

            return new AvmResult(AvmResult.Code.FAILURE, 0);
        }
    }

    /**
     * Represents a DApp module in memory.
     */
    static class DappModule {
        // Note that we currently limit the size of an in-memory JAR to 1 MiB.
        private static final int MAX_JAR_BYTES = 1024 * 1024;

        private final String mainClass;

        private Map<String, byte[]> classes;

        private ClassHierarchyForest classHierarchyForest;

        // For billing purpose
        final long bytecodeSize;
        final long numberOfClasses;

        private DappModule(Map<String, byte[]> classes, String mainClass) {
            this(classes, mainClass, null, 0, 0);
        }

        private DappModule(Map<String, byte[]> classes, String mainClass, ClassHierarchyForest classHierarchyForest, long bytecodeSize, long numberOfClasses) {
            this.classes = classes;
            this.mainClass = mainClass;
            this.classHierarchyForest = classHierarchyForest;
            this.bytecodeSize = bytecodeSize;
            this.numberOfClasses = numberOfClasses;
        }

        private static DappModule readFromJar(byte[] jar) throws IOException {
            LoadedJar loadedJar = LoadedJar.fromBytes(jar);
            ClassHierarchyForest forest = ClassHierarchyForest.createForestFrom(loadedJar);
            Map<String, byte[]> classes = loadedJar.classBytesByQualifiedNames;
            String mainClass = loadedJar.mainClassName;

            return new DappModule(classes, mainClass, forest, jar.length, classes.size());
        }

        Map<String, byte[]> getClasses() {
            return Collections.unmodifiableMap(classes);
        }

        String getMainClass() {
            return mainClass;
        }

        ClassHierarchyForest getClassHierarchyForest() {
            return classHierarchyForest;
        }

        private void setClasses(Map<String, byte[]> classes) {
            this.classes = classes;
        }

        /**
         * Create the in-memory JAR for this DApp module.
         */
        private byte[] createJar(byte[] address) throws IOException {
            // manifest
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

            // Create a temporary memory location for this JAR.
            ByteArrayOutputStream tempJarStream = new ByteArrayOutputStream(MAX_JAR_BYTES);

            // create the jar file
            JarOutputStream target = new JarOutputStream(tempJarStream, manifest);

            // add the classes
            for (String clazz : classes.keySet()) {
                JarEntry entry = new JarEntry(clazz.replace('.', '/') + ".class");
                target.putNextEntry(entry);
                target.write(classes.get(clazz));
                target.closeEntry();
            }

            // close and return
            target.close();

            return tempJarStream.toByteArray();
        }
    }

    /**
     * Sorts the user contract class names given in "classNames", alphabetically, and then looks up each of their corresponding class objects in
     * classLoader.  Note that only class names within the "user" namespace are considered.
     * 
     * @param classLoader The class loader where the classes exist.
     * @param classNames The names of the classes which should be loaded.
     * @return The class objects, in alphabetical order by their names.
     */
    private static List<Class<?>> getAlphabeticalUserTransformedClasses(AvmClassLoader classLoader, Set<String> classNames) {
        List<String> nameList = new ArrayList<>(classNames);
        Collections.sort(nameList);
        List<Class<?>> classList = new ArrayList<>();
        for (String name : nameList) {
            if (name.startsWith(PackageConstants.kUserDotPrefix)) {
                try {
                    classList.add(classLoader.loadClass(name));
                } catch (ClassNotFoundException e) {
                    // We can't fail to find something which we know we put in there.
                    RuntimeAssertionError.unexpected(e);
                }
            }
        }
        return classList;
    }
}
