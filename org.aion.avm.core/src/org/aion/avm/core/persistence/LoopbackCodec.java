package org.aion.avm.core.persistence;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;


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
    private final Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> stubWrapper;
    private Queue<Object> sequence;

    /**
     * Creates a loopback codec for memory-memory pseudo-serialization.  While it is possible to reuse these instances, it is generally recommended
     * that they be short-lived, with a new instance for each object they are serializing/deserializing.
     * 
     * @param serializer Consulted to invoke partial-automatic serialization on objects for which this is requested.
     * @param deserializer Consulted to invoke partial-automatic deserialization on objects for which this is requested.
     * @param deserializeHelper Given to the deserializer to assist with partial deserialization.  This exists because symmetric codec uses (from
     * space A to B and back to A) typically want the same core deserializer implementation, just using a slightly different mapping function.
     * @param stubWrapper Called when reading a stub - this allows it to be processed before returning.
     * This avoids duplication we would otherwise require in the deserializer implementation.
     */
    public LoopbackCodec(AutomaticSerializer serializer, AutomaticDeserializer deserializer, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> stubWrapper) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.deserializeHelper = deserializeHelper;
        this.stubWrapper = stubWrapper;
        this.sequence = new LinkedList<>();
    }

    /**
     * Extracts the queue of internal "serialized" data.  Has the consequence of reseting the internal state of the receiver.
     * Most use-cases don't want to call this, washing to leave the data fully-internal.  Some use-cases, however, may want
     * to interpret the data, directly, and can make use of this (sizing introspection, for example).
     * WARNING:  Relying on the implementation of the returned data is dangerous since it ties the caller to implementation
     * details.  Use this method with caution.
     * 
     * @return The queue of previously "serialized" data.
     */
    public Queue<Object> takeOwnershipOfData() {
        Queue<Object> extracted = this.sequence;
        this.sequence = new LinkedList<>();
        return extracted;
    }

    /**
     * Throws RuntimeAssertionError if the underlying data queue hasn't been fully drained.  This is just used as a correctness proof.
     */
    public void verifyDone() {
        RuntimeAssertionError.assertTrue(this.sequence.isEmpty());
    }

    @Override
    public void beginDeserializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.deserializer.partiallyAutoDeserialize(this.sequence, this.deserializeHelper, instance, firstManualClass);
    }

    @Override
    public byte readByte() {
        return (Byte)this.sequence.remove();
    }

    @Override
    public short readShort() {
        return (Short)this.sequence.remove();
    }

    @Override
    public char readChar() {
        return (Character)this.sequence.remove();
    }

    @Override
    public int readInt() {
        return (Integer)this.sequence.remove();
    }

    @Override
    public long readLong() {
        return (Long)this.sequence.remove();
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object readStub() {
        return this.stubWrapper.apply((org.aion.avm.shadow.java.lang.Object)this.sequence.remove());
    }

    @Override
    public void beginSerializingAutomatically(org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        this.serializer.partiallyAutoSerialize(this.sequence, instance, firstManualClass);
    }

    @Override
    public void writeByte(byte value) {
        this.sequence.add(value);
    }

    @Override
    public void writeShort(short value) {
        this.sequence.add(value);
    }

    @Override
    public void writeChar(char value) {
        this.sequence.add(value);
    }

    @Override
    public void writeInt(int value) {
        this.sequence.add(value);
    }

    @Override
    public void writeLong(long value) {
        this.sequence.add(value);
    }

    @Override
    public void writeStub(org.aion.avm.shadow.java.lang.Object object) {
        this.sequence.add(object);
    }


    public static interface AutomaticSerializer {
        public void partiallyAutoSerialize(Queue<Object> dataQueue, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);
    }


    public static interface AutomaticDeserializer {
        public void partiallyAutoDeserialize(Queue<Object> dataQueue, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass);
    }
}
