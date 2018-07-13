package org.aion.avm.internal;


/**
 * Passed to deserializeSelf() so that the receiver can abstractly deserialize itself.
 * Note that there is no identification of data elements, other than the order they are read.
 */
public interface IObjectDeserializer {
    /**
     * Called by the Object.deserializeSelf() implementation, after it deserializes its fields, to request automatic deserialization of the
     * remaining fields between (exclusive) Object and firstManualClass (all remaining classes, if null).
     * This is called after deserializing Object fields but before returning down the sub-class, where classes starting with firstManualClass
     * will manual deserialize their fields.  This means that the object's serialized form in-order from Object down to leaf class.
     * 
     * @param instance The instance to automatically deserialize.
     * @param firstManualClass The class where the automatic deserialization should stop.
     */
    void beginAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);

    byte readByte();

    short readShort();

    char readChar();

    int readInt();

    long readLong();

    org.aion.avm.shadow.java.lang.Object readStub();
}
