package org.aion.avm.core.impl;

import org.aion.avm.core.Avm;
import org.aion.avm.core.AvmResult;
import org.aion.avm.core.instrument.ClassRewriter;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.rt.BlockchainRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class AvmImpl implements Avm {
    private final Logger logger = LoggerFactory.getLogger(AvmImpl.class);

    private Class mainContractClass;

    ClassLoadingResult loadContract(String contractModulesJar, String startModuleName, String fullyQualifiedMainClassName) {
        final ModuleLayer bootLayer = ModuleLayer.boot();
        final ModuleFinder contractModulesFinder = ModuleFinder.of(Paths.get(contractModulesJar));
        final var emptyFinder = ModuleFinder.of();
        final Configuration contractLayerConfig = bootLayer.configuration().resolve(contractModulesFinder, emptyFinder, List.of(startModuleName));
        final var avmClassLoader = new AvmClassLoader();
        avmClassLoader.setModuleFinder(contractModulesFinder);
        final Function<String, ClassLoader> moduleToLoaderMapper = (name) -> avmClassLoader;
        final ModuleLayer contractLayer = bootLayer.defineModules(contractLayerConfig, moduleToLoaderMapper);
        try {
            mainContractClass = contractLayer.findModule(startModuleName)
                    .orElseThrow(() -> new Exception("Module not found"))
                    .getClassLoader()
                    .loadClass(fullyQualifiedMainClassName);
        } catch (Exception e) {
            final var msg = format("Unable to load contract. Start module:'%s', main class:'%s', module path:'%s'",
                    startModuleName, fullyQualifiedMainClassName, contractModulesJar);
            if (logger.isErrorEnabled()) {
                logger.error(msg, e);
            }
            return new ClassLoadingResult().setLoaded(false).setFailDescription(msg);
        }
        return new ClassLoadingResult().setLoaded(true);
    }

    Class getMainContractClass() {
        return mainContractClass;
    }

    /**
     * Extracts the DApp module in compressed format into the designated folder.
     *
     * @param module     the DApp module in JAR format
     * @param tempFolder the temporary folder where bytecode should be stored
     * @return the main class name if this operation is successful, otherwise null
     */
    public String extract(byte[] module, File tempFolder) {

        // TODO: Rom

        return null;
    }

    /**
     * Loads the module into memory.
     *
     * @param tempFolder the temporary folder containing all the classes
     * @return a map between class name and bytecode
     */
    public Map<String, byte[]> load(File tempFolder) {

        // TODO: ROM

        return null;
    }


    /**
     * Validates all classes, including but not limited to:
     *
     * <ul>
     * <li>class format (hash, version, etc.)</li>
     * <li>no native method</li>
     * <li>no invalid opcode</li>
     * <li>package name does not start with <code>org.aion</code></li>
     * <li>TODO: add more</li>
     * </ul>
     *
     * @param classes the classes of DApp
     * @return true if the classes are valid, otherwise false
     */
    public boolean validateClasses(Map<String, byte[]> classes) {

        // TODO: ROM

        return false;
    }

    /**
     * Returns the sizes of all the classes provided.
     *
     * @param classes the class of DApp
     * @return a mapping between class name and object size
     */
    public Map<String, Integer> calculateObjectSize(Map<String, byte[]> classes) {

        // TODO: Nancy

        return Collections.emptyMap();
    }

    /**
     * Replaces the <code>java.base</code> package with the shadow implementation.
     *
     * @param classes     the class of DApp
     * @param objectSizes the sizes of object
     * @return the classes after
     */
    public Map<String, byte[]> analyzeClasses(Map<String, byte[]> classes, Map<String, Integer> objectSizes) {

        // TODO: Yulong
        for (String name : classes.keySet()) {

            ClassReader in = new ClassReader(classes.get(name));

            // in reverse order
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassRewriter cr = new ClassRewriter(out); // inside, there is a chain of method visitors
            ClassShadowing cs = new ClassShadowing(cr);

            // traverse
            in.accept(cs, ClassReader.SKIP_DEBUG);
        }


        return null;
    }

    /**
     * Stores the instrumented bytecode into database.
     *
     * @param address   the address of the DApp
     * @param mainClass the mainclasss
     * @param classes   the instrumented bytecode
     */
    public void storeClasses(String address, String mainClass, Map<String, byte[]> classes) {

        // TODO: Rom
    }

    @Override
    public boolean deploy(byte[] code) {
        // STEP-1: compute the hash of the code, which will be used as identifier

        // STEP-2: extract the classes to a temporary folder

        // STEP-3: walk through all the classes and inject metering code

        // STEP-4: store the instrumented code and metadata(e.g. main class name)

        return false;
    }

    @Override
    public AvmResult run(byte[] codeHash, BlockchainRuntime rt) {
        // STEP-1: retrieve the instrumented bytecode using the given codeHash

        // STEP-2: load the classed. class loading fees should apply during the process

        // STEP-3: invoke the `run` method of the main class

        // STEP-4: return the DApp output


        return null;
    }
}