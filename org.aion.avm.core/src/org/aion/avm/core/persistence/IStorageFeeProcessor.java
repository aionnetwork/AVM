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
     * Called before static data is written to storage for the first time.
     * Note that all statics are stored as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void writeFirstStaticDataToStorage(int byteSize);
    /**
     * Called before static data is written to storage, updating existing statics.
     * Note that all statics are stored as one unit, so this is called once.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void writeUpdateStaticDataToStorage(int byteSize);
    /**
     * Called after an instance has been read from storage.
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void readOneInstanceFromStorage(int byteSize);
    /**
     * Called before an instance is written to storage for the first time.
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeFirstOneInstanceToStorage(int byteSize);
    /**
     * Called before an instance is written to storage, updating an existing instance.
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeUpdateOneInstanceToStorage(int byteSize);

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
     * Note that there is no "first" write for reentrant statics since the statics must be written the first time, before the DApp is callable.
     * 
     * @param byteSize The size of the static data, in bytes.
     */
    public void writeUpdateStaticDataToHeap(int byteSize);
    /**
     * Called after an instance has been read from heap (reentrant call).
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void readOneInstanceFromHeap(int byteSize);
    /**
     * Called before an instance is written to heap for the first time (reentrant call).
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeFirstOneInstanceToHeap(int byteSize);
    /**
     * Called before an instance is written to heap, updating an existing instance (reentrant call).
     * Note that this each instance is stored independently, so this is called once per instance.
     * 
     * @param byteSize The size of the instance data, in bytes.
     */
    public void writeUpdateOneInstanceToHeap(int byteSize);
}
