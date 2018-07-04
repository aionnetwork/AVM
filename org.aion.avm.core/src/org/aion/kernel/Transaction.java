package org.aion.kernel;

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
}
