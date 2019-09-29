package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.userlib.AionBuffer;
import org.aion.types.AionAddress;


public class TestingMetaEncoder {
    public static byte[] encode(MetaTransaction transaction) {
        AionBuffer buffer = AionBuffer.allocate(64 * 1024);
        
        putAddress(buffer, transaction.senderAddress);
        if (null != transaction.targetAddress) {
            // Normal transaction.
            buffer.putByte((byte)0x1);
            putAddress(buffer, transaction.targetAddress);
        } else {
            // Create.
            buffer.putByte((byte)0x0);
        }
        putBigInteger(buffer, transaction.value);
        putBytes(buffer, transaction.data);
        putBigInteger(buffer, transaction.nonce);
        putAddress(buffer, transaction.executor);
        putBytes(buffer, transaction.signature);
        
        byte[] array = buffer.getArray();
        int position = buffer.getPosition();
        byte[] result = new byte[position];
        System.arraycopy(array, 0, result, 0, position);
        return result;
    }

    public static MetaTransaction decode(byte[] serialized) {
        AionBuffer buffer = AionBuffer.wrap(serialized);
        MetaTransaction transaction = new MetaTransaction();
        
        transaction.senderAddress = getAddress(buffer);
        byte isNormal = buffer.getByte();
        transaction.targetAddress = ((byte)0x1 == isNormal)
                ? getAddress(buffer)
                : null;
        transaction.value = getBigInteger(buffer);
        transaction.data = getBytes(buffer);
        transaction.nonce = getBigInteger(buffer);
        transaction.executor = getAddress(buffer);
        transaction.signature = getBytes(buffer);
        
        return transaction;
    }


    public static class MetaTransaction {
        public AionAddress senderAddress;
        public AionAddress targetAddress;
        public BigInteger value;
        public byte[] data;
        public BigInteger nonce;
        public AionAddress executor;
        public byte[] signature;
    }


    private static void putAddress(AionBuffer buffer, AionAddress address) {
        buffer.put(address.toByteArray());
    }

    private static AionAddress getAddress(AionBuffer buffer) {
        byte[] temp = new byte[AionAddress.LENGTH];
        buffer.get(temp);
        return new AionAddress(temp);
    }

    private static void putBigInteger(AionBuffer buffer, BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 32) {
            throw new AssertionError("Test serializer limits BigInteger size");
        }
        buffer.putByte((byte) bytes.length);
        buffer.put(bytes);
    }

    private static BigInteger getBigInteger(AionBuffer buffer) {
        byte length = buffer.getByte();
        if (length > 32) {
            throw new AssertionError("Test serializer limits BigInteger size");
        }
        byte[] temp = new byte[length];
        buffer.get(temp);
        return new BigInteger(temp);
    }

    private static void putBytes(AionBuffer buffer, byte[] bytes) {
        int length = bytes.length;
        buffer.putByte((byte) (0xff & (length >> 24)));
        buffer.putByte((byte) (0xff & (length >> 16)));
        buffer.putByte((byte) (0xff & (length >> 8)));
        buffer.putByte((byte) (0xff & length));
        buffer.put(bytes);
    }

    private static byte[] getBytes(AionBuffer buffer) {
        int length = (0xFF000000 & (buffer.getByte() << 24))
                | (0x00FF0000 & (buffer.getByte() << 16))
                | (0x0000FF00 & (buffer.getByte() << 8))
                | (0x000000FF & buffer.getByte())
                ;
        byte[] temp = new byte[length];
        buffer.get(temp);
        return temp;
    }
}
