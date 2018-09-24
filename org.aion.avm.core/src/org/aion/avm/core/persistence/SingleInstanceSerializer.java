package org.aion.avm.core.persistence;

import java.util.function.Consumer;

import org.aion.avm.internal.IObjectSerializer;


/**
 * One of these objects is created for each object instance we are serializing.
 * It is basically a thin wrapper over ExtentBasedCodec.Encoder.
 * It is intended to just serialize one instance so any other instances it sees from this one are given to the nextObjectSink.
 */
public class SingleInstanceSerializer implements IObjectSerializer {
    private final IAutomatic automaticPart;
    private final ExtentBasedCodec.Encoder encoder;
    private final Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink;

    public SingleInstanceSerializer(IAutomatic automaticPart, ExtentBasedCodec.Encoder encoder, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        this.automaticPart = automaticPart;
        this.encoder = encoder;
        this.nextObjectSink = nextObjectSink;
    }

    @Override
    public void beginSerializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.automaticPart.partialAutomaticSerializeInstance(this.encoder, instance, firstManualClass, this.nextObjectSink);
    }

    @Override
    public void writeByte(byte value) {
        this.encoder.encodeByte(value);
    }

    @Override
    public void writeShort(short value) {
        this.encoder.encodeShort(value);
    }

    @Override
    public void writeChar(char value) {
        this.encoder.encodeChar(value);
    }

    @Override
    public void writeInt(int value) {
        this.encoder.encodeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.encoder.encodeLong(value);
    }

    @Override
    public void writeStub(org.aion.avm.shadow.java.lang.Object object) {
        this.automaticPart.encodeAsStub(this.encoder, object, this.nextObjectSink);
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
        void partialAutomaticSerializeInstance(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue);

        /**
         * Encodes the given object in the given encoder as a reference stub, passing the instance into nextObjectQueue if the receiver determines that it need to be serialized.
         * This is only called at the end of an invocation, when the graph is being serialized for storage.
         * 
         * @param encoder The encoder to use.
         * @param object The object instance which should be encoded as a stub.
         * @param nextObjectQueue The queue which will accept the object, if the receiver determines it also needs to be serialized.
         */
        void encodeAsStub(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue);
    }
}
