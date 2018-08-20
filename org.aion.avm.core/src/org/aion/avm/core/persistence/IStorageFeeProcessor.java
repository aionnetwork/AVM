package org.aion.avm.core.persistence;


/**
 * The interface which describes how to bill for storage.
 * This interface may expose more precision than actually matters (details of storage/heap, for example) so it may be simplified in the future.
 * For now, it makes the uses of this very explicit, so specific introspection into its uses is easier.
 */
public interface IStorageFeeProcessor {
    /**
     * Called after static data has been read from storage.
     * Note that all statics are loaded as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void readStaticDataFromStorage(int byteSize);
    /**
     * Called before static data is written to storage.
     * Note that all statics are stored as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void writeStaticDataToStorage(int byteSize);
    /**
     * Called after an instance has been read from storage.
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void readOneInstanceFromStorage(int byteSize);
    /**
     * Called before an instance is written to storage.
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeOneInstanceToStorage(int byteSize);

    /**
     * Called after static data has been read from heap (reentrant call).
     * Note that all statics are loaded as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void readStaticDataFromHeap(int byteSize);
    /**
     * Called before static data is written to heap (reentrant call).
     * Note that all statics are stored as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void writeStaticDataToHeap(int byteSize);
    /**
     * Called after an instance has been read from heap (reentrant call).
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void readOneInstanceFromHeap(int byteSize);
    /**
     * Called before an instance is written to heap (reentrant call).
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeOneInstanceToHeap(int byteSize);
}
