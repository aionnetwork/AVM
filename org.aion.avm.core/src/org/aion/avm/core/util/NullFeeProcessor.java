package org.aion.avm.core.util;

import org.aion.avm.core.persistence.IStorageFeeProcessor;


/**
 * IStorageFeeProcessor for utilities:  bills nothing.
 * This implementation is used for unit tests and offline tooling which have no notion of billing and failure due to exhausted energy.
 */
public class NullFeeProcessor implements IStorageFeeProcessor {
    @Override
    public void readStaticDataFromStorage(int byteSize) {
    }

    @Override
    public void writeFirstStaticDataToStorage(int byteSize) {
    }

    @Override
    public void writeUpdateStaticDataToStorage(int byteSize) {
    }

    @Override
    public void readOneInstanceFromStorage(int byteSize) {
    }

    @Override
    public void writeFirstOneInstanceToStorage(int byteSize) {
    }

    @Override
    public void writeUpdateOneInstanceToStorage(int byteSize) {
    }

    @Override
    public void readStaticDataFromHeap(int byteSize) {
    }

    @Override
    public void writeUpdateStaticDataToHeap(int byteSize) {
    }

    @Override
    public void readOneInstanceFromHeap(int byteSize) {
    }

    @Override
    public void writeFirstOneInstanceToHeap(int byteSize) {
    }

    @Override
    public void writeUpdateOneInstanceToHeap(int byteSize) {
    }
}
