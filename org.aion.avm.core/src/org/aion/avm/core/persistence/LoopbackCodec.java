package org.aion.avm.core.persistence;

import java.util.function.Function;

import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;


/**
 * This is used in the reentrant call support to support "deserialization" of the caller space into the callee space and, on commit,
 * the "serialization" of the callee objects back into their caller counterparts.
 * The idea is that primitives are completely unchanged (just boxed/unboxed as part of a generic data queue) while objects are
 * passed through a deserialization helper (which may wrap, unwrap, etc the object).
 * While most uses of this treat is as a self-contained serializer-deserializer, it is possible to access the queue of "serialized"
 * data via the takeOwnershipOfData() call.
 */
public class LoopbackCodec implements IObjectSerializer, IObjectDeserializer {
    private final AutomaticSerializer serializer;
    private final AutomaticDeserializer deserializer;
    private final Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper;
    private HeapRepresentationCodec.Encoder encoder;
    private HeapRepresentationCodec.Decoder decoder;

    /**
     * Creates a loopback codec for memory-memory pseudo-serialization.  While it is possible to reuse these instances, it is generally recommended
     * that they be short-lived, with a new instance for each object they are serializing/deserializing.
     * 
     * @param serializer Consulted to invoke partial-automatic serialization on objects for which this is requested.
     * @param deserializer Consulted to invoke partial-automatic deserialization on objects for which this is requested.
     * @param deserializeHelper Given to the deserializer to assist with partial deserialization.  This exists because symmetric codec uses (from
     * space A to B and back to A) typically want the same core deserializer implementation, just using a slightly different mapping function.
     * This must be non-null if we expect to do any deserializing.
     */
    public LoopbackCodec(AutomaticSerializer serializer, AutomaticDeserializer deserializer, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.deserializeHelper = deserializeHelper;
        this.encoder = new HeapRepresentationCodec.Encoder();
    }

    public HeapRepresentation takeOwnershipOfData() {
        HeapRepresentation representation = this.encoder.toHeapRepresentation();
        this.encoder = null;
        return representation;
    }

    public void switchToDecode() {
        HeapRepresentation representation = this.encoder.toHeapRepresentation();
        this.encoder = null;
        this.decoder = new HeapRepresentationCodec.Decoder(representation);
    }

    @Override
    public void beginDeserializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.deserializer.partiallyAutoDeserialize(this.decoder, this.deserializeHelper, instance, firstManualClass);
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
        // We assume that if we have a non-null decoder object, we must have a non-null deserializeHelper object
        return this.deserializeHelper.apply(this.decoder.decodeReference());
    }

    @Override
    public void beginSerializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.serializer.partiallyAutoSerialize(this.encoder, instance, firstManualClass);
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
        this.encoder.encodeReference(object);
    }


    public static interface AutomaticSerializer {
        public void partiallyAutoSerialize(HeapRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);
    }


    public static interface AutomaticDeserializer {
        public void partiallyAutoDeserialize(HeapRepresentationCodec.Decoder decoder, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);
    }
}
