package org.aion.avm.internal;

import java.util.function.Consumer;


/**
 * Passed to serializeSelf() so that the receiver can abstractly serialize itself.
 * Note that there is no identification of data elements, other than the order they are written.
 */
public interface IObjectSerializer {
    /**
     * Called by the Object.serializeSelf() implementation, after it serializes its fields, to request automatic serialization of the
     * remaining fields between (exclusive) Object and firstManualClass (all remaining classes, if null).
     * This is called after serializing Object fields but before returning down the sub-class, where classes starting with firstManualClass
     * will manual serialize their fields.  This means that the object's serialized form in-order from Object down to leaf class.
     * 
     * @param instance The instance to automatically serialize.
     * @param firstManualClass The class where the automatic serialization should stop.
     * @param nextObjectQueue The queue which will accept any other object instances found while serializing instance.
     */
    void beginSerializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue);

    void writeByte(byte value);

    void writeShort(short value);

    void writeChar(char value);

    void writeInt(int value);

    void writeLong(long value);

    void writeStub(org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue);
}
