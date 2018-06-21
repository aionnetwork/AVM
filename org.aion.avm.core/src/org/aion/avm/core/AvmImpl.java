package org.aion.avm.core;

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
import org.aion.avm.core.miscvisitors.StringConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.InvalidTxDataException;
import org.aion.avm.core.util.TxDataDecoder;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.FatalAvmError;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.IAvmProxy;
import org.aion.kernel.TransformedDappStorage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

public class AvmImpl implements Avm, IAvmProxy {
    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);
    private static final String HELPER_CLASS = PackageConstants.kInternalSlashPrefix + "Helper";

    /**
     * We will re-use this top-level class loader for all contracts as the classes within it are state-less and have no dependencies on a contract.
     * It is provided by the caller of our constructor, meaning it gets to decide if the same AvmImpl is reused, or not.
     */
    private final AvmSharedClassLoader sharedClassLoader;

    /**
     * The TransformedDappStorage is unique and provided by the kernel.
     */
    private final TransformedDappStorage codeStorage;

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

    public AvmImpl(AvmSharedClassLoader sharedClassLoader, TransformedDappStorage codeStorage) {
        this.sharedClassLoader = sharedClassLoader;
        this.codeStorage = codeStorage;
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
        map.put("java/lang/Enum", 4);

        // TODO (issue-79):  Implement the rest of these by walking the runtime, elsewhere (this "4" is probably not right, in most cases).
        map.put("java/lang/AssertionError", 4);
        map.put("java/lang/Throwable", 4);
        map.put("java/lang/Exception", 4);
        map.put("java/lang/RuntimeException", 4);
        map.put("java/lang/NullPointerException", 4);

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
    public static Map<String, Integer> computeObjectSizes(Forest<String, byte[]> classHierarchy, Map<String, Integer> runtimeObjectSizes) {
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
     * @param classes        the class of DApp (names specified in .-style)
     * @param preRenameClassHierarchy The pre-rename hierarchy of user-defined classes in the DApp (/-style).
     * @param preRenameObjectSizes The sizes of object by their pre-rename names (/-style).
     * @return the transformed classes and any generated classes (names specified in .-style)
     */
    public Map<String, byte[]> transformClasses(Map<String, byte[]> classes, Forest<String, byte[]> preRenameClassHierarchy, Map<String, Integer> preRenameObjectSizes) {

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
        ClassWhiteList classWhiteList = new ClassWhiteList();
        Set<String> preRenameUserDefinedClasses = ClassWhiteList.extractDeclaredClasses(preRenameClassHierarchy);
        ParentPointers parentClassResolver = new ParentPointers(preRenameUserDefinedClasses, preRenameClassHierarchy);
        
        // The size map contains both the user-defined and the common types so we will start with the original and change it.
        Map<String, Integer> postRenameObjectSizes = new HashMap<>(preRenameObjectSizes);
        for (String prename : preRenameUserDefinedClasses) {
            String slashPre = Helpers.fulllyQualifiedNameToInternalName(prename);
            postRenameObjectSizes.remove(slashPre);
            postRenameObjectSizes.put(PackageConstants.kUserSlashPrefix + slashPre, preRenameObjectSizes.get(slashPre));
        }
        
        for (String name : classes.keySet()) {
            // Note that transformClasses requires that the input class names by the .-style names.
            Assert.assertTrue(-1 == name.indexOf("/"));

            byte[] bytecode = new ClassToolchain.Builder(classes.get(name), ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new UserClassMappingVisitor(preRenameUserDefinedClasses))
                    .addNextVisitor(new RejectionClassVisitor(classWhiteList))
                    .addNextVisitor(new StringConstantVisitor())
                    .addNextVisitor(new ClassMetering(HELPER_CLASS, postRenameObjectSizes))
                    .addNextVisitor(new InvokedynamicShadower(HELPER_CLASS, PackageConstants.kShadowJavaLangSlashPrefix, classWhiteList))
                    .addNextVisitor(new ClassShadowing(HELPER_CLASS, classWhiteList))
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

    /**
     * A helper method to match the method selector with the main-class methods.
     */
    public Method matchMethodSelector(Class<?> clazz, String methodName, String argsDescriptor) {
        Method[] methods = clazz.getMethods();

        // We only allow Java primitive types or 1D/2D array of the primitive types in the parameter list.
        Map<Character, String[]> elementaryTypesMap = new HashMap<>();
        elementaryTypesMap.put(TxDataDecoder.BYTE,      new String[]{"B", "byte", "ByteArray"});
        elementaryTypesMap.put(TxDataDecoder.BOOLEAN,   new String[]{"Z", "boolean", "ByteArray"});
        elementaryTypesMap.put(TxDataDecoder.CHAR,      new String[]{"C", "char", "CharArray"});
        elementaryTypesMap.put(TxDataDecoder.SHORT,     new String[]{"S", "short", "ShortArray"});
        elementaryTypesMap.put(TxDataDecoder.INT,       new String[]{"I", "int", "IntArray"});
        elementaryTypesMap.put(TxDataDecoder.FLOAT,     new String[]{"F", "float", "FloatArray"});
        elementaryTypesMap.put(TxDataDecoder.LONG,      new String[]{"J", "long", "LongArray"});
        elementaryTypesMap.put(TxDataDecoder.DOUBLE,    new String[]{"D", "double", "DoubleArray"});

        String ARRAY_WRAPPER_PREFIX = "org.aion.avm.arraywrapper.";

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
                            if (pType.charAt(0) == '[' || pType.charAt(0) == '$') {
                                pType = pType.substring(1);
                            } else if (pType.startsWith(ARRAY_WRAPPER_PREFIX)) {
                                pType = pType.substring(ARRAY_WRAPPER_PREFIX.length());
                            } else {
                                matched = false;
                                break;
                            }

                            if (argsDescriptor.length() - idx < 2) {
                                matched = false;
                                break;
                            }

                            char eType;
                            if (argsDescriptor.charAt(++idx) == TxDataDecoder.ARRAY_S) {
                                if (pType.charAt(0) == '$') {
                                    pType = pType.substring(1);
                                }
                                else {
                                    matched = false;
                                    break;
                                }
                                eType = argsDescriptor.charAt(++idx);
                                idx = argsDescriptor.indexOf(TxDataDecoder.ARRAY_E, idx);
                            }
                            else {
                                eType = argsDescriptor.charAt(idx);
                                idx = argsDescriptor.indexOf(TxDataDecoder.ARRAY_E, idx);
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

    /**
     * Convert the method call arguments, 1) take care of the array wrapping; 2) convert to an array list.
     *
     * @param arguments
     * @return
     */
    private Object[] convertArguments(List<Object> arguments) {
        for (int index = 0; index < arguments.size(); index ++) {
            Object obj = arguments.get(index);
            if (obj.getClass().isArray()) {
                switch (obj.getClass().getName()) {
                    case "[B":
                        obj = new ByteArray((byte[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[C":
                        obj = new CharArray((char[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[D":
                        obj = new DoubleArray((double[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[F":
                        obj = new FloatArray((float[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[I":
                        obj = new IntArray((int[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[J":
                        obj = new LongArray((long[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[S":
                        obj = new ShortArray((short[]) obj);
                        arguments.set(index, obj);
                        break;
                    case "[Z":
                        obj = new ByteArray((byte[]) obj);
                        arguments.set(index, obj);
                        break;
                    default:
                        break;
                }
            }
        }
        return arguments.toArray();
    }

    public static Map<String, Integer> computeAllObjectsSizes(Forest<String, byte[]> dappClasses){
        return computeObjectSizes(dappClasses, computeRuntimeObjectSizes());
    }

    @Override
    public AvmResult deploy(byte[] jar, org.aion.avm.java.lang.String codeVersion, BlockchainRuntime rt) {
        try {
            // read dapp module
            DappModule app = readDapp(jar);
            if (app == null) {
                return new AvmResult(AvmResult.Code.INVALID_JAR, 0);
            }

            // validate dapp module
            if (!validateDapp(app)) {
                return new AvmResult(AvmResult.Code.INVALID_CODE, 0);
            }
            ClassHierarchyForest dappClassesForest = app.getClassHierarchyForest();
            // transform
            Map<String, byte[]> transformedClasses = transformClasses(app.getClasses(), dappClassesForest, computeAllObjectsSizes(dappClassesForest));
            app.setClasses(transformedClasses);

            // As per usual, we need to get the special Helper class for each contract loader.
            Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(transformedClasses);

            // Construct the per-contract class loader and access the per-contract IHelper instance.
            AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
            IHelper helper = Helpers.instantiateHelper(classLoader,  rt);

            // billing the Processing cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                    + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * app.bytecodeSize * (1 + app.numberOfClasses) / 10);

            // Parse the tx data, get the method name and arguments
            TxDataDecoder txDataDecoder = new TxDataDecoder();
            if (rt.avm_getData() != null) {
                TxDataDecoder.MethodCaller methodCaller = txDataDecoder.decode(rt.avm_getData().getUnderlying());
                String newMethodName = UserClassMappingVisitor.mapMethodName(methodCaller.methodName);
                String newArgDescriptor = methodCaller.argsDescriptor; // TODO: nancy, take array wrapping into consideration

                String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
                Class<?> clazz = classLoader.loadClass(mappedUserMainClass);
                Object obj = clazz.getConstructor().newInstance();

                // select the method and invoke
                Method method = matchMethodSelector(clazz, newMethodName, newArgDescriptor);
                if (methodCaller.arguments == null) {
                    method.invoke(obj);
                } else {
                    method.invoke(obj, convertArguments(methodCaller.arguments));
                }
            }

            // store transformed dapp
            File transformedDappJar = app.createJar(rt.avm_getAddress());
            TransformedDappStorage.CodeVersion codeVersionFormat = null;
            if (codeVersion == null) {
                codeVersionFormat = TransformedDappStorage.CodeVersion.VERSION_1_0;
            }
            else {
                codeStorage.matchCodeVersion(codeVersion.toString());
            }
            if (codeVersionFormat == null) {
                throw new InvalidTxDataException(); // code version is not accepted.
            }
            codeStorage.storeCode(rt.avm_getAddress().unwrap(), codeVersionFormat, transformedDappJar);

            // billing the Storage cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * jar.length);

            return new AvmResult(AvmResult.Code.SUCCESS, rt.avm_getEnergyLimit(), rt.avm_getAddress());
        } catch (FatalAvmError e) {
            // These are unrecoverable errors (either a bug in our code or a lower-level error reported by the JVM).
            // (for now, we System.exit(-1), since this is what ethereumj does, but we may want a more graceful shutdown in the future)
            e.printStackTrace();
            System.exit(-1);
            return null;
        } catch (OutOfEnergyError e) {
            return new AvmResult(AvmResult.Code.OUT_OF_ENERGY, 0);
        } catch (InvalidTxDataException | UnsupportedEncodingException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
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
        // retrieve the transformed bytecode
        DappModule app;
        try {
            File transformedDappJar = codeStorage.loadCode(rt.avm_getAddress().unwrap());
            app = DappModule.readFromJar(Helpers.readFileToBytes(transformedDappJar.getPath()));
        } catch (IOException e) {
            return new AvmResult(AvmResult.Code.INVALID_CALL, 0);
        }

        // As per usual, we need to get the special Helper class for each contract loader.
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);

        // Construct the per-contract class loader and access the per-contract IHelper instance.
        AvmClassLoader classLoader = new AvmClassLoader(this.sharedClassLoader, allClasses);
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, true, classLoader);
        classLoader.addHandler(wrapperGenerator);
        IHelper helper = Helpers.instantiateHelper(classLoader,  rt);

        // load class
        try {
            // Parse the tx data, get the method name and arguments
            TxDataDecoder txDataDecoder = new TxDataDecoder();
            if (rt.avm_getData() == null) {
                throw new InvalidTxDataException();
            }
            TxDataDecoder.MethodCaller methodCaller = txDataDecoder.decode(rt.avm_getData().getUnderlying());
            String newMethodName = UserClassMappingVisitor.mapMethodName(methodCaller.methodName);
            String newArgDescriptor = methodCaller.argsDescriptor; // TODO: nancy, take array wrapping into consideration

            String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
            Class<?> clazz = classLoader.loadClass(mappedUserMainClass);
            // At a contract call, only choose the one without arguments.
            Object obj = clazz.getConstructor().newInstance();

            // generate the method descriptor of each main class method, compare to the method selector to select or invalidate the txData
            Method method = matchMethodSelector(clazz, newMethodName, newArgDescriptor);
            Object ret;
            if (methodCaller.arguments == null) {
                ret = method.invoke(obj);
            }
            else {
                ret = method.invoke(obj, convertArguments(methodCaller.arguments));
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

    @Override
    public AvmResult removeDapp(Address beneficiary, BlockchainRuntime rt) {
        codeStorage.removeDapp(rt.getAddress().unwrap());
        // TODO - ask the kernel to send the remaining balance to the beneficiary.

        return new AvmResult(AvmResult.Code.SUCCESS, 0);
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
        private File createJar(Address address) throws IOException {
            // manifest
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

            // file name from the address
            DAPPS_DIR.mkdirs();
            File jarFile = new File(DAPPS_DIR, addressToString(address) + ".jar");

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

        private static final char[] hexArray = "0123456789abcdef".toCharArray();

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
    }
}
