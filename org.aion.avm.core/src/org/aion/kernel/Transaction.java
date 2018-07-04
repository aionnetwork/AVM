package org.aion.kernel;

import org.aion.avm.core.util.Helpers;

import java.util.Arrays;

public class Transaction {

    public enum Type {
        CREATE, CALL
    }

    Type type;

    byte[] from;

    byte[] to;

    byte[] value;

    byte[] data;

    long energyLimit;

    public Transaction(Type type, byte[] from, byte[] to, byte[] value, byte[] data, long energyLimit) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
    }

    public Type getType() {
        return type;
    }

    public byte[] getFrom() {
        return from;
    }

    public byte[] getTo() {
        return to;
    }

    public byte[] getValue() {
        return value;
    }

    public byte[] getData() {
        return data;
    }

    public long getEnergyLimit() {
        return energyLimit;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", from=" + Helpers.toHexString(Arrays.copyOf(from, 4)) +
                ", to=" + Helpers.toHexString(Arrays.copyOf(to, 4)) +
                ", value=" + Helpers.toHexString(value) +
                ", data=" + Helpers.toHexString(data) +
                ", energyLimit=" + energyLimit +
                '}';
    }
}
