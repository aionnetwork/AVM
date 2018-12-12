package org.aion.avm.core;

import org.aion.avm.core.instrument.BytecodeFeeScheduler;


/**
 * A container class which includes static routines to calculate more complex fees based on multiple factors.
 * This makes these easier to scrutinize, test, and update.
 */
public class BillingRules {
    public static final int BASIC_COST = 21_000;
    
    
    /**
     * The cost of deploying a contract is based on the number of classes (as they represent JVM resources) and the size of the code.
     * 
     * @param numberOfClassesProvided The number of classes defined in the jar.
     * @param sizeOfJarInBytes The physical size of the jar (its compressed size).
     * @return The total fee for such a deployment.
     */
    public static long getDeploymentFee(long numberOfClassesProvided, long sizeOfJarInBytes) {
        // This logic was extracted directly from DAppCreator.
        return BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * sizeOfJarInBytes
                * (1 + numberOfClassesProvided) / 10;
    }
    
    /**
     * The cost of storing a program with the given code size.
     * 
     * @param sizeOfJarInBytes The physical size of the jar (its compressed size).
     * @return The total fee for storing this code.
     */
    public static long getCodeStorageFee(long sizeOfJarInBytes) {
        // This logic was extracted directly from DAppCreator.
        return BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * sizeOfJarInBytes;
    }
    
    /**
     * The basic cost of a transaction including the given data.
     * 
     * @return The basic cost, in energy, of a transaction with this data.
     */
    public static long getBasicTransactionCost(byte[] transactionData) {
        int cost = BASIC_COST;
        for (byte b : transactionData) {
            cost += (b == 0) ? 4 : 64;
        }
        return cost;
    }
}
