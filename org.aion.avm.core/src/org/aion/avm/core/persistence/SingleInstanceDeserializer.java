package org.aion.avm.core.persistence;

import org.aion.avm.internal.IObjectDeserializer;


/**
 * One of these objects is created for each object instance we are deserializing.
 * It is basically a thin wrapper over SerializedRepresentationCodec.Decoder.
 */
public class SingleInstanceDeserializer implements IObjectDeserializer {
    private final IAutomatic automaticPart;
    private final SerializedRepresentationCodec.Decoder decoder;

    public SingleInstanceDeserializer(IAutomatic automaticPart, SerializedRepresentationCodec.Decoder decoder) {
        this.automaticPart = automaticPart;
        this.decoder = decoder;
    }

    @Override
    public void beginDeserializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.automaticPart.partialAutomaticDeserializeInstance(this.decoder, instance, firstManualClass);
    }

    @Override
    public byte readByte() {
        return this.decoder.decodeByte();
    }

    @Override
    public short readShort() {
        return this.decoder.decodeShort();
    }

    @Override
    public char readChar() {
        return this.decoder.decodeChar();
    }

    @Override
    public int readInt() {
        return this.decoder.decodeInt();
    }

    @Override
    public long readLong() {
        return this.decoder.decodeLong();
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object readStub() {
        return this.automaticPart.decodeStub(this.decoder.decodeReference());
    }


    /**
     * This is the interface we must be given to handle the "automatic" part of the deserialization.
     * This has to come through us since we own the decoder.
     */
    public static interface IAutomatic {
        /**
         * Requests that the given instance be partially automatically deserialized:  the receiver is responsible for automatic deserialization of
         * all field defined between (exclusive) the shadow Object and firstManualClass as shadow Object provides special handling for its fields
         * and firstManualClass (and all sub-classes) manually deserialize their fields.
         * 
         * @param decoder The decoder to use.
         * @param instance The object instance to deserialize.
         * @param firstManualClass This class, and all sub-classes, will manually deserialize their declared fields (if null, the entire object is automatic).
         */
        void partialAutomaticDeserializeInstance(SerializedRepresentationCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);

        /**
         * Decodes the next data in the given decoder as though it were an instance stub.  The object returned might be a stub or may be fully-inflated.
         * Note that all references to the same stub see references to the same instance, when decoded.
         * This is called during lazy loading of an instance which has a reference type instance variable.
         * 
         * @param node The node to decode as an instance.
         * @return The new instance described by the stub (could be null if the stub described a null).
         */
        org.aion.avm.shadow.java.lang.Object decodeStub(INode node);
    }
}
