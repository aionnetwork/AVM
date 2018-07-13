package org.aion.avm.core.persistence;

import java.util.function.Consumer;

import org.aion.avm.internal.IObjectSerializer;


/**
 * One of these objects is created for each object instance we are serializing.
 * It is basically a thin wrapper over StreamingPrimitiveCodec.Encoder.
 */
public class SingleInstanceSerializer implements IObjectSerializer {
    private final IAutomatic automaticPart;
    private final StreamingPrimitiveCodec.Encoder encoder;

    public SingleInstanceSerializer(IAutomatic automaticPart, StreamingPrimitiveCodec.Encoder encoder) {
        this.automaticPart = automaticPart;
        this.encoder = encoder;
    }

    @Override
    public void beginAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        this.automaticPart.partialAutomaticSerializeInstance(this.encoder, instance, firstManualClass, nextObjectQueue);
    }

    @Override
    public void writeInt(int value) {
        this.encoder.encodeInt(value);
    }


    /**
     * This is the interface we must be given to handle the "automatic" part of the serialization.
     * This has to come through us since we own the encoder.
     */
    public static interface IAutomatic {
        /**
         * Requests that the given instance be partially automatically serialized:  the receiver is responsible for automatic serialization of all
         * field defined between (exclusive) the shadow Object and firstManualClass as shadow Object provides special handling for its fields and
         * firstManualClass (and all sub-classes) manually serialize their fields.
         * 
         * @param encoder The encoder to use.
         * @param instance The object instance to serialize.
         * @param firstManualClass This class, and all sub-classes, will manually serialize their declared fields (if null, the entire object is automatic).
         * @param nextObjectQueue The queue which will accept any other object instances found while serializing instance.
         */
        void partialAutomaticSerializeInstance(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue);
    }
}
