package org.aion.avm.rt;

public interface Contract {

    /**
     * Executes this start smart with the given input.
     *
     * @param input the input
     * @param rt    the runtime
     * @return the output
     */
    byte[] run(byte[] input, BlockchainRuntime rt);
}