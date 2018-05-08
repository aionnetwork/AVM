package org.aion.avm.core.impl;

import org.aion.avm.core.Avm;
import org.aion.avm.core.AvmClassLoader;
import org.aion.avm.core.AvmResult;
import org.aion.avm.rt.BlockchainRuntime;

import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class AvmImpl implements Avm {
    // todo make it null after contract has been executed. To allow classes to be unloaded
    private final AvmClassLoader avmClassLoader = new AvmClassLoader();

    private Class mainContractClass;

    public void computeContract(String contractModulesPath, String startModuleName, String fullyQualifiedMainClassName) {
        loadContract(contractModulesPath, startModuleName, fullyQualifiedMainClassName);
    }

    private void loadContract(String contractModulesPath, String startModuleName, String fullyQualifiedMainClassName) {
        final var bootLayer = ModuleLayer.boot();
        final var contractModulesFinder = ModuleFinder.of(Paths.get(contractModulesPath));
        final var emptyFinder = ModuleFinder.of();
        final var contractLayerConfig = bootLayer.configuration().resolve(contractModulesFinder, emptyFinder, List.of(startModuleName));
        final var contractLayer = bootLayer.defineModulesWithOneLoader(contractLayerConfig, avmClassLoader);
        try {
            mainContractClass = contractLayer.findModule(startModuleName)
                    .orElseThrow(() -> new Exception("Module not found"))
                    .getClassLoader()
                    .loadClass(fullyQualifiedMainClassName);
        } catch (Exception e) {
            final var msg = format("Unable to load contract. Start module:'%s', main class:'%s', module path:'%s'",
                    startModuleName, fullyQualifiedMainClassName, contractModulesPath);
            throw new IllegalStateException(msg, e);
        }
    }

    public Class getMainContractClass() {
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