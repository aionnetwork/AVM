package org.aion.kernel;

import org.aion.avm.core.Avm;


/**
 * Interface for accessing kernel features.
 */
public interface KernelInterface {
    void putTransformedCode(byte[] address, DappCode.CodeVersion version, byte[] code);

    byte[] getTransformedCode(byte[] address);

    void putStorage(byte[] address, byte[] key, byte[] value);

    byte[] getStorage(byte[] address, byte[] key);

    /**
     * Called to run another transaction, inline within the currently-running transaction.
     * Note that the transaction may need to run on the same sourceVm (since they should be reused), which is reentrant.
     * 
     * @param sourceVm The VM where the call originated (note that run() is reentrant).
     * @param internalTx The transaction which was created for this call.
     * @param parentBlock The block where the parent transaction was running.
     * @return The result of the call.
     */
    TransactionResult call(Avm sourceVm, InternalTransaction internalTx, Block parentBlock);

    void updateCode(byte[] address, byte[] code);

    void selfdestruct(byte[] address, byte[] beneficiary);
}
