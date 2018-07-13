package org.aion.avm.core.persistence;

import org.aion.avm.internal.IObjectDeserializer;


/**
 * One of these objects is created for each object instance we are deserializing.
 * It is basically a thin wrapper over StreamingPrimitiveCodec.Decoder.
 * Note that this implementation is just a first-cut to show the flow through the system.  It will later be expanded to do decoding of other types.
 */
public class SingleInstanceDeserializer implements IObjectDeserializer {
    private final StreamingPrimitiveCodec.Decoder decoder;

    public SingleInstanceDeserializer(StreamingPrimitiveCodec.Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public int readInt() {
        return this.decoder.decodeInt();
    }
}
