package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;

public interface Contract {
    /**
     * Entry point of execution at deployment with the given input.
     *
     * This method is executed only once at the deployment. It takes the parameters that are transmitted in the transaction data field.
     * Normally, the user implements this method to initialize the state of the contract and put it into the storage. The runtime provides the APIs to access the storage.
     *
     * @param input the input
     * @param rt    the runtime
     * @return the output
     */
    ByteArray contractCreation(ByteArray input, BlockchainRuntime rt);

    /**
     * Entry point of execution at every call to this smart contract with the given input.
     *
     * @param input the input
     * @param rt    the runtime
     * @return the output
     */
    ByteArray run(ByteArray input, BlockchainRuntime rt);
}