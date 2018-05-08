package org.aion.avm.core;

import org.aion.avm.rt.BlockchainRuntime;

/**
 * High-level Aion Virtual Machine interface.
 *
 * @author Roman Katerinenko
 * @author Yulong
 */
public interface Avm {

    /**
     * Deploys a DApp. This allows the VM to prepare/instrument the classes.
     *
     * @param code the Java bytecode in jar format
     * @return true if the code was successfully deployed.
     */
    boolean deploy(byte[] code);

    /**
     * Executes the given DApp, with the provided runtime.
     *
     * @param codeHash the code identifier
     * @param rt       the executing runtime
     * @return the result
     */
    AvmResult run(byte[] codeHash, BlockchainRuntime rt);
}