package org.aion.avm.core;

/**
 * @author Roman Katerinenko
 */
public interface Avm {
    void computeContract(String pathToContractModule, String startModuleName, String fullyQualifiedClassName) throws Exception;
}