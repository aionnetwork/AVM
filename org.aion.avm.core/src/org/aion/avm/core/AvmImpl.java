package org.aion.avm.core;

import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.arraywrapper.*;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.instrument.HeapMemoryCostCalculator;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.InvalidTxDataException;
import org.aion.avm.internal.*;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApi;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransformedDappStorage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.aion.avm.core.FileUtils.getFSRootDirFor;
import static org.aion.avm.core.FileUtils.putToTempDir;

public class AvmImpl implements Avm {
    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);
    private static final String HELPER_CLASS = PackageConstants.kInternalSlashPrefix + "Helper";

    private static final TransformedDappStorage.CodeVersion VERSION = TransformedDappStorage.CodeVersion.VERSION_1_0;

    /**
     * We will re-use this top-level class loader for all contracts as the classes within it are state-less and have no dependencies on a contract.
     * It is provided by the caller of our constructor, meaning it gets to decide if the same AvmImpl is reused, or not.
     */
    private final AvmSharedClassLoader sharedClassLoader;

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

    public AvmImpl(AvmSharedClassLoader sharedClassLoader) {
        this.sharedClassLoader = sharedClassLoader;
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
        map.put("org/aion/avm/api/ABIDecoder", 4);
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
        ParentPointers parentClassResolver = new ParentPointers(preRenameUserDefinedClasses, preRenameClassHierarchy);
        Map<String, Integer> postRenameObjectSizes = computeAllPostRenameObjectSizes(preRenameClassHierarchy);

        for (String name : classes.keySet()) {
            // Note that transformClasses requires that the input class names by the .-style names.
            Assert.assertTrue(-1 == name.indexOf("/"));

            byte[] bytecode = new ClassToolchain.Builder(classes.get(name), ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new RejectionClassVisitor())
                    .addNextVisitor(new UserClassMappingVisitor(preRenameUserDefinedClasses))
                    .addNextVisitor(new ConstantVisitor(HELPER_CLASS))
                    .addNextVisitor(new ClassMetering(HELPER_CLASS, postRenameObjectSizes))
                    .addNextVisitor(new InvokedynamicShadower(PackageConstants.kShadowSlashPrefix))
                    .addNextVisitor(new ClassShadowing(HELPER_CLASS))
                    .addNextVisitor(new StackWatcherClassAdapter())
                    .addNextVisitor(new ExceptionWrapping(HELPER_CLASS, parentClassResolver, generatedClassesSink))
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this.sharedClassLoader, parentClassResolver, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            bytecode = new ClassToolchain.Builder(bytecode, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this.sharedClassLoader, parentClassResolver, dynamicHierarchyBuilder))
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

        public BlockchainRuntimeImpl(Transaction tx, Block block, KernelApi cb) {
            this.tx = tx;
            this.block = block;
            this.cb = cb;
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
        public ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray data, long energyLimit) {
            AvmResult result = cb.call(tx.getTo(), targetAddress.unwrap(), value.getUnderlying(), data.getUnderlying(), energyLimit);

            return new ByteArray(result.returnData);
        }

        @Override
        public void avm_log(ByteArray index0, ByteArray data) {
            cb.log(tx.getTo(), index0.getUnderlying(), data.getUnderlying());
        }
    }

    public static IBlockchainRuntime createBlockchainRuntime(Transaction tx, Block block, KernelApi cb) {
        return new BlockchainRuntimeImpl(tx, block , cb);
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
            byte[] dappAddress = tx.getTo(); // TODO: CREATE does not specify address
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
            AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
            IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit());
            Helpers.attachBlockchainRuntime(classLoader, createBlockchainRuntime(tx, block, cb));

            // billing the Processing cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                    + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * app.bytecodeSize * (1 + app.numberOfClasses) / 10);

            // store transformed dapp
            File transformedDappJar = app.createJar(dappAddress);
            cb.putTransformedCode(dappAddress, VERSION, transformedDappJar);

            // billing the Storage cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * tx.getData().length);

            // TODO: create invocation is temporarily disabled

            return new AvmResult(AvmResult.Code.SUCCESS, tx.getEnergyLimit(), new Address(dappAddress));
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
        DappModule app;
        try {
            File transformedDappJar = cb.getTransformedCode(tx.getTo());
            app = DappModule.readFromJar(Helpers.readFileToBytes(transformedDappJar.getPath()));
        } catch (IOException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
        }

        // As per usual, we need to get the special Helper class for each contract loader.
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);

        // Construct the per-contract class loader and access the per-contract IHelper instance.
        AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, classLoader);
        classLoader.addHandler(wrapperGenerator);
        IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit());
        Helpers.attachBlockchainRuntime(classLoader, createBlockchainRuntime(tx, block, cb));

        // load class
        try {
            if (tx.getData() == null) {
                throw new InvalidTxDataException();
            }

            String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
            Class<?> clazz = classLoader.loadClass(mappedUserMainClass);
            // At a contract call, only choose the one without arguments.
            Object obj = clazz.getConstructor().newInstance();

            // call contract main method
            Method method = clazz.getMethod("avm_main");
            byte[] ret = ((ByteArray) method.invoke(obj)).getUnderlying();

            return new AvmResult(AvmResult.Code.SUCCESS, helper.externalGetEnergyRemaining(), ret);
        } catch (InvalidTxDataException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
        }catch (InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
            return new AvmResult(AvmResult.Code.FAILURE, 0);
        } catch (OutOfEnergyError e) {
            return new AvmResult(AvmResult.Code.OUT_OF_ENERGY, 0);
        }catch (Exception e) {
            e.printStackTrace();

            return new AvmResult(AvmResult.Code.FAILURE, 0);
        }
    }

    /**
     * Represents a DApp module in memory.
     */
    static class DappModule {
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
            ClassHierarchyForest forest = ClassHierarchyForest.createForestFrom(jar);
            Map<String, byte[]> classes = forest.toFlatMapWithoutRoots();
            String mainClass = readMainClassQualifiedNameFrom(jar);

            return new DappModule(classes, mainClass, forest, jar.length, classes.size());
        }

        private static String readMainClassQualifiedNameFrom(byte[] jar) throws IOException {
            final Path pathToJar = putToTempDir(jar, "aiontemp", "module-temp.jar");
            final Path rootInJar = getFSRootDirFor(pathToJar);
            final var container = new ArrayList<String>(1);
            Files.walkFileTree(rootInJar, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().equalsIgnoreCase("MANIFEST.MF")) {
                        container.add(extractMainClassNameFrom(file));
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return container.size() == 1 ? container.get(0) : null;
        }

        private static String extractMainClassNameFrom(Path file) {
            final var propertyKey = "Main-Class";
            try (InputStream in = Files.newInputStream(file)) {
                final var properties = new Properties();
                properties.load(in);
                Object result = properties.get(propertyKey);
                return result.toString();
            } catch (IOException e) {
                logger.debug(String.format("Can't find property %s in jar %s", propertyKey, file));
            }
            return null;
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

        private static final File DAPPS_DIR = new File("../dapps");

        /**
         * Create a jar file from this Dapp module.
         */
        private File createJar(byte[] address) throws IOException {
            // manifest
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

            // file name from the address
            DAPPS_DIR.mkdirs();
            File jarFile = new File(DAPPS_DIR, Helpers.toHexString(address) + ".jar");

            // create the jar file
            JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile), manifest);

            // add the classes
            for (String clazz : classes.keySet()) {
                JarEntry entry = new JarEntry(clazz.replace('.', '/') + ".class");
                target.putNextEntry(entry);
                target.write(classes.get(clazz));
                target.closeEntry();
            }

            // close and return
            target.close();

            return jarFile;
        }
    }
}
