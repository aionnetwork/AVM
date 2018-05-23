package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.instrument.HeapMemoryCostCalculator;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.rt.BlockchainRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.aion.avm.core.FileUtils.getFSRootDirFor;
import static org.aion.avm.core.FileUtils.putToTempDir;

public class AvmImpl implements Avm {
    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);
    private static final String HELPER_CLASS = "org/aion/avm/internal/Helper";

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
     * <li>package name does not start with <code>org.aion</code></li>
     * <li>main class is a <code>Contract</code></li>
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
     * @return
     */
    public Map<String, Integer> computeRuntimeObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("java/lang/Object", 4);
        map.put("java/lang/Class", 4);
        map.put("java/lang/Math", 4);
        map.put("java/lang/String", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the sizes of all the classes, including the runtime ones and the DApp ones.
     *
     * @param classHierarchy     the class hierarchy
     * @param runtimeObjectSizes the object size of runtime classes
     * @return a mapping between class name and object size, for all classes, including the runtime ones from "runtimeObjectSizes"; and the DApp ones passed-in with "classes".
     */
    public Map<String, Integer> computeObjectSizes(ClassHierarchyForest classHierarchy, Map<String, Integer> runtimeObjectSizes) {
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
    public Map<String, byte[]> transformClasses(Map<String, byte[]> classes, ClassHierarchyForest classHierarchy, Map<String, Integer> objectSizes) {

        Map<String, byte[]> processedClasses = new HashMap<>();
        Map<String, byte[]> generatedClasses = new HashMap<>();

        for (String name : classes.keySet()) {
            ClassReader in = new ClassReader(classes.get(name));

            /*
             * CLASS_READER => CLASS_METERING => STACK_TRACKING => CLASS_SHADOWING => EXCEPTION_WRAPPING => ARRAY_WRAPPING => CLASS_WRITER
             */

            // in reverse order
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ArrayWrappingClassAdapter arrayWrapping = new ArrayWrappingClassAdapter(out);
            ExceptionWrapping exceptionHandling = new ExceptionWrapping(arrayWrapping, HELPER_CLASS, classHierarchy, generatedClasses);
            ClassShadowing classShadowing = new ClassShadowing(exceptionHandling, HELPER_CLASS);
            StackWatcherClassAdapter stackTracking = new StackWatcherClassAdapter(classShadowing);
            ClassMetering classMetering = new ClassMetering(stackTracking, HELPER_CLASS, classHierarchy, objectSizes);

            // traverse
            // TODO:  ClassReader.EXPAND_FRAMES is needed for stacktracking injector
            in.accept(classMetering, ClassReader.EXPAND_FRAMES);

            // emit bytecode
            processedClasses.put(name, out.toByteArray());
        }

        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        processedClasses.putAll(generatedClasses);

        return processedClasses;
    }

    /**
     * Stores the instrumented bytecode into database.
     *
     * @param address the address of the DApp
     * @param dapp    the dapp module
     */
    public void storeTransformedDapp(ByteArray address, DappModule dapp) {

        // TODO: Rom
    }

    public DappModule loadTransformedDapp(ByteArray address) {

        // TODO: Rom

        return null;
    }

    @Override
    public AvmResult deploy(byte[] module, BlockchainRuntime rt) {

        try {
            // read dapp module
            DappModule app = readDapp(module);
            if (app == null) {
                return new AvmResult(AvmResult.Code.INVALID_JAR, 0);
            }

            // validate dapp module
            if (!validateDapp(app)) {
                return new AvmResult(AvmResult.Code.INVALID_CODE, 0);
            }

            // compute object sizes
            Map<String, Integer> runtimeObjectSizes = computeRuntimeObjectSizes();
            Map<String, Integer> objectSizes = computeObjectSizes(app.getClassHierarchyForest(), runtimeObjectSizes);
            objectSizes.putAll(runtimeObjectSizes);

            // transform
            Map<String, byte[]> transformedClasses = transformClasses(app.getClasses(), app.getClassHierarchyForest(), objectSizes);
            app.setClasses(transformedClasses);

            // store transformed dapp
            storeTransformedDapp(rt.getAddress(), app);

            // TODO: billing

            return new AvmResult(AvmResult.Code.SUCCESS, rt.getEnergyLimit());
        } catch (Exception e) {
            return new AvmResult(AvmResult.Code.INVALID_CODE, 0);
        }
    }

    @Override
    public AvmResult run(BlockchainRuntime rt) {
        //  retrieve the transformed bytecode
        DappModule app = loadTransformedDapp(rt.getAddress());

        // TODO: create a class loader and load the main class

        // TODO: create an instance and invoke the `run` method

        // TODO: return the result

        return null;
    }

    /**
     * Represents a DApp module in memory.
     */
    static class DappModule {
        private final String mainClass;

        private Map<String, byte[]> classes;

        private ClassHierarchyForest classHierarchyForest;

        private static DappModule readFromJar(byte[] jar) throws IOException {
            ClassHierarchyForest forest = ClassHierarchyForest.createForestFrom(jar);
            Map<String, byte[]> classes = forest.toFlatMapWithoutRoots();
            String mainClass = readMainClassQualifiedNameFrom(jar);

            return new DappModule(classes, mainClass, forest);
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

        private DappModule(Map<String, byte[]> classes, String mainClass, ClassHierarchyForest classHierarchyForest) {
            this.classes = classes;
            this.mainClass = mainClass;
            this.classHierarchyForest = classHierarchyForest;
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
