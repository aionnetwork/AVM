package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.java.lang.String;


/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {

    /**
     * Returns the sender address.
     *
     * @return
     */
    Address avm_getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    Address avm_getAddress();

    /**
     * Returns the energy limit.
     *
     * @return
     */
    long avm_getEnergyLimit();

    /**
     * Returns the transaction data.
     * @return
     */
    ByteArray avm_getData();

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    ByteArray avm_getStorage(ByteArray key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void avm_putStorage(ByteArray key, ByteArray value);

    /**
     * Update the Dapp code with a new version.
     *
     * @param newCode
     * @param codeVersion
     */
    void avm_updateCode(ByteArray newCode, String codeVersion);

    /**
     * Destruct the Dapp.
     *
     * @param beneficiary
     */
    void avm_selfDestruct(Address beneficiary);
}
