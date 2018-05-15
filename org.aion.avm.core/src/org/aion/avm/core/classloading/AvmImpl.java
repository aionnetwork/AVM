package org.aion.avm.core.classloading;

import org.aion.avm.core.Avm;
import org.aion.avm.core.AvmResult;
import org.aion.avm.core.instrument.ClassRewriter;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.ClassHierarchyForest;
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
@Deprecated
public class AvmImpl {
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
}