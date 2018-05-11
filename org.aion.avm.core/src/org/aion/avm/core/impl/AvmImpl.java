package org.aion.avm.core.impl;

import org.aion.avm.core.Avm;
import org.aion.avm.core.AvmResult;
import org.aion.avm.rt.BlockchainRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.List;
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