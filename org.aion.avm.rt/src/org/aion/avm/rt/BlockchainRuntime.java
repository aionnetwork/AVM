package org.aion.avm.rt;

/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {

    /**
     * Returns the sender address.
     *
     * @return
     */
    byte[] getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    byte[] getAddress();

    /**
     * Returns the energy limit.
     *
     * @return
     */
    long getEnergyLimit();

    /**
     * Returns the storage of the executing account.
     *
     * @return
     */
    Storage getStorage();
}
