package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;

public interface Contract {
    /**
     * Executes this start smart with the given input.
     *
     * @param input the input
     * @param rt    the runtime
     * @return the output
     */
    ByteArray run(ByteArray input, BlockchainRuntime rt);
}