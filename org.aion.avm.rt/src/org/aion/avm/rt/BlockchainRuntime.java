package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.java.lang.String;


/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {
    // Runtime-facing implementation.
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

    // Compiler-facing implementation.
    default Address getSender() { return avm_getSender(); }

    default Address getAddress() { return avm_getAddress(); }

    default long getEnergyLimit() { return avm_getEnergyLimit(); }

    default byte[] getData() { return avm_getData().getUnderlying(); }

    default byte[] getStorage(byte[] key) { return avm_getStorage(new ByteArray(key)).getUnderlying(); }

    default void putStorage(byte[] key, byte[] value) { avm_putStorage(new ByteArray(key), new ByteArray(value)); }

    default void updateCode(byte[] newCode, java.lang.String codeVersion) { avm_updateCode(new ByteArray(newCode), new String(codeVersion)); }

    default void selfDestruct(Address beneficiary) { avm_selfDestruct(beneficiary); }
}
