package org.aion.avm.internal;


/**
 * Passed to the stub constructor of an object which exists in storage.
 * This is used by said object to request that it be inflated to a full object.
 */
public interface IDeserializer {
    /**
     * Called by the shadow Object lazyLoad() routine to request that it be deserialized.
     * This call will result in deserializeSelf() being invoked on that instance.
     * 
     * @param instance The instance to load.
     * @param instanceId The persistence identifier of instance.
     */
    void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, long instanceId);
}
