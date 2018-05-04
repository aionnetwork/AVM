package org.aion.avm.core.impl;

import org.aion.avm.core.Avm;
import org.aion.avm.core.AvmClassLoader;

import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class AvmImpl implements Avm {
    private final AvmClassLoader avmClassLoader = new AvmClassLoader();

    private Class mainContractClass;

    @Override
    public void computeContract(String contractModulesPath, String startModuleName, String fullyQualifiedMainClassName) throws Exception {
        loadContract(contractModulesPath, startModuleName, fullyQualifiedMainClassName);
    }

    private void loadContract(String contractModulesPath, String startModuleName, String fullyQualifiedMainClassName) throws Exception {
        final var bootLayer = ModuleLayer.boot();
        final var contractModulesFinder = ModuleFinder.of(Paths.get(contractModulesPath));
        final var emptyFinder = ModuleFinder.of();
        final var contractLayerConfig = bootLayer.configuration().resolve(contractModulesFinder, emptyFinder, List.of(startModuleName));
        final var contractLayer = bootLayer.defineModulesWithOneLoader(contractLayerConfig, avmClassLoader);
        final var contractModuleClassLoader = contractLayer.findModule(startModuleName).orElseThrow(() -> new Exception("Module not found")).getClassLoader();
        mainContractClass = contractModuleClassLoader.loadClass(fullyQualifiedMainClassName);
    }

    public Class getMainContractClass() {
        return mainContractClass;
    }

    public static class ContractLoadingException extends Exception {

        public ContractLoadingException(String contractModulesPath, String startModuleName, String mainClass) {
            super(format("Unable to load contract. Start module:'%s', main class:'%s', module path:'%s'", startModuleName, mainClass, contractModulesPath));
        }

    }
}