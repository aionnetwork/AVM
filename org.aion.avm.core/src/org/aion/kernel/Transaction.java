package org.aion.kernel;

public class Transaction {

    public enum Type {
        CREATE, CALL
    }

    Type type;

    byte[] from;

    byte[] to;

    byte[] payload;

    long energyLimit;

    public Transaction(Type type, byte[] from, byte[] to, byte[] payload, long energyLimit) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.payload = payload;
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

    public byte[] getPayload() {
        return payload;
    }

    public long getEnergyLimit() {
        return energyLimit;
    }
}
