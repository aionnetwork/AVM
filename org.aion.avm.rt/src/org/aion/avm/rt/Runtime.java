package org.aion.avm.rt;

/**
 * Represents the hub of AVM runtime.
 */
public interface Runtime {

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
     * Returns the storage of the executing account.
     *
     * @return
     */
    Storage getStorage();

    /**
     * Returns the energy meter.
     *
     * @return
     */
    EnergyMeter getEnergyMeter();
}
