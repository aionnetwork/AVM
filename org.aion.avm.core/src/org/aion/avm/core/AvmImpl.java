package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.instrument.HeapMemoryCostCalculator;
import org.aion.avm.core.miscvisitors.StringConstantVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.InvalidTxDataException;
import org.aion.avm.core.util.TxDataDecoder;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.FatalAvmError;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;

import static org.aion.avm.core.FileUtils.getFSRootDirFor;
import static org.aion.avm.core.FileUtils.putToTempDir;

public class AvmImpl implements Avm {
    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);
    private static final String HELPER_CLASS = "org/aion/avm/internal/Helper";
    private static final File DAPPS_DIR = new File("../dapps");

    /**
     * We will re-use this top-level class loader for all contracts as the classes within it are state-less and have no dependencies on a contract.
     * It is provided by the caller of our constructor, meaning it gets to decide if the same AvmImpl is reused, or not.
     */
    private final AvmSharedClassLoader sharedClassLoader;

    static {
        DAPPS_DIR.mkdirs();
    }

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
     * <li>no access to any <code>org.aion.avm</code> packages but the <code>org.aion.avm.rt</code> package</li>
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
     * Computes the object size of runtime classes
     *
     * @return a mapping between class name and object size
     *
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public static Map<String, Integer> computeRuntimeObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("java/lang/Object", 4);
        map.put("java/lang/Class", 4);
        map.put("java/lang/Math", 4);
        map.put("java/lang/String", 4);

        // TODO (issue-79):  Implement the rest of these by walking the runtime, elsewhere (this "4" is probably not right, in most cases).
        map.put("java/lang/AssertionError", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the sizes of all the classes, including the runtime ones and the DApp ones.
     *
     * @param classHierarchy     the class hierarchy
     * @param runtimeObjectSizes the object size of runtime classes
     * @return a mapping between class name and object size, for all classes, including the runtime ones from "runtimeObjectSizes"; and the DApp ones passed-in with "classes".
     *
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public Map<String, Integer> computeObjectSizes(Forest<String, byte[]> classHierarchy, Map<String, Integer> runtimeObjectSizes) {
        HeapMemoryCostCalculator objectSizeCalculator = new HeapMemoryCostCalculator();

        // copy over the runtime classes sizes
        Map<String, Integer> objectSizes = new HashMap<>(runtimeObjectSizes);

        // compute the object size of every one in 'classes'
        objectSizeCalculator.calcClassesInstanceSize(classHierarchy, runtimeObjectSizes);

        // copy over the DApp classes sizes
        objectSizes.putAll(objectSizeCalculator.getClassHeapSizeMap());

        return Collections.unmodifiableMap(objectSizes);
    }

    /**
     * Replaces the <code>java.base</code> package with the shadow implementation.
     *
     * @param classes        the class of DApp
     * @param classHierarchy the class hierarchy
     * @param objectSizes    the sizes of object
     * @return the transformed classes and any generated classes
     */
    public Map<String, byte[]> transformClasses(Map<String, byte[]> classes, Forest<String, byte[]> classHierarchy, Map<String, Integer> objectSizes) {

        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        Map<String, byte[]> processedClasses = new HashMap<>();
        // WARNING:  This dynamicHierarchyBuilder is both mutable and shared by TypeAwareClassWriter instances.
        HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        // We also want to expose this type to the class writer so it can compute common superclasses.
        ExceptionWrapping.GeneratedClassConsumer generatedClassesSink = (superClassSlashName, classSlashName, bytecode) -> {
            processedClasses.put(classSlashName, bytecode);
            dynamicHierarchyBuilder.addClass(classSlashName, superClassSlashName, bytecode);
        };
        ClassWhiteList classWhiteList = ClassWhiteList.buildFromClassHierarchy(classHierarchy);
        for (String name : classes.keySet()) {
            byte[] bytecode = new ClassToolchain.Builder(classes.get(name), ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new RejectionClassVisitor(classWhiteList))
                    .addNextVisitor(new StringConstantVisitor())
                    .addNextVisitor(new ClassMetering(HELPER_CLASS, objectSizes))
                    .addNextVisitor(new ClassShadowing(HELPER_CLASS, classWhiteList))
                    .addNextVisitor(new StackWatcherClassAdapter())
                    .addNextVisitor(new ExceptionWrapping(HELPER_CLASS, classHierarchy, generatedClassesSink))
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this.sharedClassLoader, classHierarchy, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            bytecode = new ClassToolchain.Builder(bytecode, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this.sharedClassLoader, classHierarchy, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            processedClasses.put(name, bytecode);
        }
        return processedClasses;
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private String addressToString(Address address) {
        byte[] bytes = address.unwrap();
        int length = bytes.length;

        char[] hexChars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Stores the instrumented bytecode into database.
     *
     * TODO: re-design this dummy implementation
     *
     * @param address the address of the DApp
     * @param dapp    the dapp module
     * @return the stored bytecode size
     */
    public long storeTransformedDapp(Address address, DappModule dapp) {
        long size = 0;
        String id = addressToString(address);
        File dir = new File(DAPPS_DIR, id);
        dir.mkdir();

        // store main class
        File main = new File(dir, "MAIN");
        Helpers.writeBytesToFile(dapp.getMainClass().getBytes(), main.getAbsolutePath());
        size += dapp.getMainClass().getBytes().length;

        // store bytecode
        Map<String, byte[]> classes = dapp.getClasses();
        for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
            File file = new File(dir, entry.getKey() + ".class");
            Helpers.writeBytesToFile(entry.getValue(), file.getAbsolutePath());
            size += entry.getValue().length;
        }

        return size;
    }

    /**
     * Loads the transformed bytecode.
     *
     * TODO: re-design this dummy implementation
     *
     * @param address
     * @return
     */
    public DappModule loadTransformedDapp(Address address) {
        String id = addressToString(address);
        File dir = new File(DAPPS_DIR, id);
        if (!dir.exists()) {
            return null;
        }

        // store main class
        File file = new File(dir, "MAIN");
        String mainClass = new String(Helpers.readFileToBytes(file.getAbsolutePath()));

        // store bytecode
        Map<String, byte[]> classes = new HashMap<>();
        for (String fileName : dir.list()) {
            if (fileName.endsWith(".class")) {
                String name = fileName.substring(0, fileName.length() - 6);
                byte[] bytes = Helpers.readFileToBytes(new File(dir, fileName).getAbsolutePath());
                classes.put(name, bytes);
            }
        }

        return new DappModule(classes, mainClass);
    }

    /**
     * A helper method to match the method selector with the main-class methods.
     */
    public Method matchMethodSelector(Class<?> clazz, String methodName, String argsDescriptor) {
        Method[] methods = clazz.getMethods();

        // We only allow Java primitive types or 1D/2D array of the primitive types in the parameter list.
        Map<Character, String[]> elementaryTypesMap = new HashMap<>();
        elementaryTypesMap.put(TxDataDecoder.BYTE,      new String[]{"B", "byte"});
        elementaryTypesMap.put(TxDataDecoder.BOOLEAN,   new String[]{"Z", "boolean"});
        elementaryTypesMap.put(TxDataDecoder.CHAR,      new String[]{"C", "char"});
        elementaryTypesMap.put(TxDataDecoder.SHORT,     new String[]{"S", "short"});
        elementaryTypesMap.put(TxDataDecoder.INT,       new String[]{"I", "int"});
        elementaryTypesMap.put(TxDataDecoder.FLOAT,     new String[]{"F", "float"});
        elementaryTypesMap.put(TxDataDecoder.LONG,      new String[]{"J", "long"});
        elementaryTypesMap.put(TxDataDecoder.DOUBLE,    new String[]{"D", "double"});

        methodName = ClassShadowing.METHOD_PREFIX + methodName;

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes == null || parameterTypes.length == 0) && (argsDescriptor==null || argsDescriptor.isEmpty())) {
                    return method;
                }

                int parIdx = 0;
                boolean matched = true;
                for (int idx = 0; idx < argsDescriptor.length(); idx++) {
                    char c = argsDescriptor.charAt(idx);
                    switch (c) {
                        case TxDataDecoder.ARRAY_S:
                            String pType = parameterTypes[parIdx].getName();
                            if (pType.charAt(0) == '[') {
                                pType = pType.substring(1);
                            }
                            else {
                                break;
                            }

                            if (argsDescriptor.length() - idx < 2) {
                                matched = false;
                                break;
                            }

                            char eType;
                            if (argsDescriptor.charAt(++idx) == TxDataDecoder.ARRAY_S) {
                                if (pType.charAt(0) == '[') {
                                    pType = pType.substring(1);
                                }
                                else {
                                    break;
                                }
                                eType = argsDescriptor.charAt(++idx);
                                idx += 2;
                            }
                            else {
                                eType = argsDescriptor.charAt(idx);
                                idx ++;
                            }

                            if (pType.charAt(0) == 'L') {
                                pType = pType.substring(1);
                            }

                            if (!(Arrays.asList(elementaryTypesMap.get(eType)).contains(pType))) {
                                matched = false;
                                break;
                            }
                            break;
                        default:
                            if (!(Arrays.asList(elementaryTypesMap.get(c)).contains(parameterTypes[parIdx].getName()))) {
                                matched = false;
                                break;
                            }
                    }
                    if (!matched) {
                        break;
                    }
                    else {
                        parIdx ++;
                    }
                }
                if (matched) {
                    return method;
                }
            }
        }
        return null;
    }

    @Override
    public AvmResult deploy(byte[] jar, BlockchainRuntime rt) {
        try {
            // read dapp module
            DappModule app = readDapp(jar);
            if (app == null) {
                return new AvmResult(AvmResult.Code.INVALID_JAR, 0);
            }

            // As per usual, we need to get the special Helper class for each contract loader.
            Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);
            
            // Construct the per-contract class loader and access the per-contract IHelper instance.
            AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
            IHelper helper = Helpers.instantiateHelper(classLoader,  rt);

            // billing the Processing cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            try {
                helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                                    + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * app.bytecodeSize * (1 + app.numberOfClasses) / 10);
            } catch (OutOfEnergyError e) {
                return new AvmResult(AvmResult.Code.OUT_OF_ENERGY, 0);
            }

            // validate dapp module
            if (!validateDapp(app)) {
                return new AvmResult(AvmResult.Code.INVALID_CODE, 0);
            }

            // compute object sizes
            Map<String, Integer> runtimeObjectSizes = computeRuntimeObjectSizes();
            Map<String, Integer> allObjectSizes = computeObjectSizes(app.getClassHierarchyForest(), runtimeObjectSizes);

            // transform
            Map<String, byte[]> transformedClasses = transformClasses(app.getClasses(), app.getClassHierarchyForest(), allObjectSizes);
            app.setClasses(transformedClasses);

            // TODO: parse the txData, select and execute the main-class method as the user requires (a match with the txData method selector)

            // store transformed dapp
            long storedSize = storeTransformedDapp(rt.avm_getAddress(), app);

            // billing the Storage cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * storedSize);

            return new AvmResult(AvmResult.Code.SUCCESS, rt.avm_getEnergyLimit());
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

    @Override
    public AvmResult run(BlockchainRuntime rt) {
        //  retrieve the transformed bytecode
        DappModule app = loadTransformedDapp(rt.avm_getAddress());

        // As per usual, we need to get the special Helper class for each contract loader.
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);
        
        // Construct the per-contract class loader and access the per-contract IHelper instance.
        AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName);
        classLoader.addHandler(wrapperGenerator);
        IHelper helper = Helpers.instantiateHelper(classLoader,  rt);

        // load class
        try {
            // Parse the tx data, get the method name and arguments
            TxDataDecoder txDataDecoder = new TxDataDecoder();
            TxDataDecoder.MethodCaller methodCaller = txDataDecoder.decode(rt.avm_getData().getUnderlying());

            Class<?> clazz = classLoader.loadClass(app.mainClass);
            // At a contract call, only choose the one without arguments.
            Object obj = clazz.getConstructor().newInstance();

            // generate the method descriptor of each main class method, compare to the method selector to select or invalidate the txData
            Method method = matchMethodSelector(clazz, methodCaller.methodName, methodCaller.argsDescriptor);
            Object ret;
            if (methodCaller.arguments == null) {
                ret = method.invoke(obj);
            }
            else {
                ret = method.invoke(obj, methodCaller.arguments.toArray());
            }

            // TODO: energy left
            return new AvmResult(AvmResult.Code.SUCCESS, helper.externalGetEnergyRemaining(), ret);
        } catch (InvalidTxDataException | UnsupportedEncodingException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
        } catch (Exception e) {
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
    }
}
