package org.aion.avm.core;

import org.aion.avm.core.persistence.IStorageFeeProcessor;
import org.aion.avm.internal.IHelper;


/**
 * An implementation of the storage fees which bills costs directly to the IHelper instance for the corresponding call frame.
 * 
 * TODO:  Determine if we want to continue using the IHelper of the current call frame or the thread local IHelper from the top.
 * How we interpret this has serious implications into how storage billing is done in reentrant DApps.
 * 
 * Implications of using to corresponding call frame (current approach):
 * -each operation corresponds directly to when data becomes visible or committed within the context of the corresponding call
 * -this "visibility" may not have been triggered by the corresponding call, but a sub-call
 *  -this is due to the rule that a sub-calls faulted graph must always be a subset of the caller's faulted graph
 * -this means that a sub-call can actually cause its caller to run out of energy
 *  -forcing the caller to fault in the object bills their IHelper, not one currently running
 */
public class HelperBasedStorageFees implements IStorageFeeProcessor {
    // (these are only public so that tests can access them)
    public static final long FIXED_READ_COST = 1_000L;
    public static final long FIXED_WRITE_COST = 10_000L;
    public static final long BYTE_READ_COST = 1L;
    public static final long BYTE_WRITE_COST = 1L;

    private final IHelper helper;

    public HelperBasedStorageFees(IHelper helper) {
        this.helper = helper;
    }

    @Override
    public void readStaticDataFromStorage(int byteSize) {
        long cost = FIXED_READ_COST + (BYTE_READ_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeFirstStaticDataToStorage(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeUpdateStaticDataToStorage(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void readOneInstanceFromStorage(int byteSize) {
        long cost = FIXED_READ_COST + (BYTE_READ_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeFirstOneInstanceToStorage(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeUpdateOneInstanceToStorage(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void readStaticDataFromHeap(int byteSize) {
        long cost = FIXED_READ_COST + (BYTE_READ_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeUpdateStaticDataToHeap(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void readOneInstanceFromHeap(int byteSize) {
        long cost = FIXED_READ_COST + (BYTE_READ_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeFirstOneInstanceToHeap(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }

    @Override
    public void writeUpdateOneInstanceToHeap(int byteSize) {
        long cost = FIXED_WRITE_COST + (BYTE_WRITE_COST * (long)byteSize);
        helper.externalChargeEnergy(cost);
    }
}
