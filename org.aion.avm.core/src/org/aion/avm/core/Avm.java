package org.aion.avm.core;

import org.aion.avm.rt.BlockchainRuntime;

/**
 * High-level Aion Virtual Machine interface.
 *
 * @author Roman Katerinenko
 * @author Yulong
 */
public interface Avm {

    // TODO: refactor, merge the following two methods

    /**
     * Deploys a DApp. This allows the VM to prepare/instrument the classes.
     *
     * @param module the dapp code in JAR format
     * @param rt     the blockchain runtime
     * @return the result
     */
    AvmResult deploy(byte[] module, org.aion.avm.java.lang.String codeVersion, BlockchainRuntime rt);

    /**
     * Executes the given DApp, with the provided runtime.
     *
     * @param rt   the blockchain runtime
     * @return the result
     */
    AvmResult run(BlockchainRuntime rt);
}