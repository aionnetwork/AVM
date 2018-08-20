package org.aion.avm.core.util;

import org.aion.avm.core.persistence.IStorageFeeProcessor;


/**
 * IStorageFeeProcessor for utilities:  bills nothing.
 * This implementation is used for unit tests and offline tooling which have no notion of billing and failure due to exhausted energy.
 */
public class NullFeeProcessor implements IStorageFeeProcessor {
    @Override
    public void readStaticDataFromStorage(int length) {
    }

    @Override
    public void writeStaticDataToStorage(int length) {
    }

    @Override
    public void readOneInstanceFromStorage(int byteSize) {
    }

    @Override
    public void writeOneInstanceToStorage(int byteSize) {
    }

    @Override
    public void readStaticDataFromHeap(int length) {
    }

    @Override
    public void writeStaticDataToHeap(int length) {
    }

    @Override
    public void readOneInstanceFromHeap(int byteSize) {
    }

    @Override
    public void writeOneInstanceToHeap(int byteSize) {
    }
}
