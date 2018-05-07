package org.aion.avm.rt;

public abstract class Contract {

    /**
     * Executes this start smart with the given input.
     *
     * @param input   the input encoded in some ABI, not NULL
     * @param context the execution context.
     * @return the output
     */
    public abstract byte[] run(byte[] input, Context context);

}
