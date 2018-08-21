package org.aion.avm.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Many of our classes are serialized using similar mechanisms so this class exists to contain those implementations and avoid duplication.
 */
public final class CodecIdioms {
    private static final Charset SERIALIZATION_CHARSET = StandardCharsets.UTF_8;

    public static String deserializeString(IObjectDeserializer deserializer) {
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        byte[] data = new byte[length];
        for (int i = 0; i < length; ++i) {
            data[i] = deserializer.readByte();
        }
        return new String(data, SERIALIZATION_CHARSET);
    }

    public static void serializeString(IObjectSerializer serializer, String string) {
        // TODO:  We probably want faster array copies.
        byte[] data = string.getBytes(SERIALIZATION_CHARSET);
        serializer.writeInt(data.length);
        for (int i = 0; i < data.length; ++i) {
            serializer.writeByte(data[i]);
        }
    }

    public static byte[] deserializeByteArray(IObjectDeserializer deserializer) {
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        byte[] array = new byte[length];
        for (int i = 0; i < length; ++i) {
            array[i] = deserializer.readByte();
        }
        return array;
    }

    public static void serializeByteArray(IObjectSerializer serializer, byte[] array) {
        // TODO:  We probably want faster array copies.
        serializer.writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            serializer.writeByte(array[i]);
        }
    }
}
