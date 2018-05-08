package org.aion.avm.rt;

public interface EnergyMeter {

    /**
     * Returns the remaining energy.
     *
     * @return
     */
    long energyLeft();

    /**
     * Returns the total energy that has been consumed.
     * @return
     */
    long energyUsed();

    /**
     * Consumes the given energy.
     *
     * @param nrg
     * @return
     */
    long consume(long nrg) throws OutOfEnergyException;
}
