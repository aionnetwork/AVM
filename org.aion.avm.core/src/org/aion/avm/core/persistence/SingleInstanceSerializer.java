package org.aion.avm.core.persistence;

import org.aion.avm.internal.IObjectSerializer;


/**
 * One of these objects is created for each object instance we are serializing.
 * It is basically a thin wrapper over StreamingPrimitiveCodec.Encoder.
 * Note that this implementation is just a first-cut to show the flow through the system.  It will later be expanded to do encoding of other types.
 */
public class SingleInstanceSerializer implements IObjectSerializer {
    private final StreamingPrimitiveCodec.Encoder encoder;

    public SingleInstanceSerializer(StreamingPrimitiveCodec.Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void writeInt(int value) {
        this.encoder.encodeInt(value);
    }
}
