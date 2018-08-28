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

    long nonce;

    long value;

    byte[] data;

    long energyLimit;

    long energyPrice;

    public Transaction(Type type, byte[] from, byte[] to, long nonce, long value, byte[] data, long energyLimit, long energyPrice) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
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

    public long getNonce() {
        return nonce;
    }

    public long getValue() {
        return value;
    }

    public byte[] getData() {
        return data;
    }

    public long getEnergyLimit() {
        return energyLimit;
    }

    public long getEnergyPrice() {
        return energyPrice;
    }

    public int getBasicCost() {
        int cost = 21_000;
        for (byte b : getData()) {
            cost += (b == 0) ? 4 : 64;
        }
        return cost;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", from=" + Helpers.toHexString(Arrays.copyOf(from, 4)) +
                ", to=" + Helpers.toHexString(Arrays.copyOf(to, 4)) +
                ", value=" + value +
                ", data=" + Helpers.toHexString(data) +
                ", energyLimit=" + energyLimit +
                '}';
    }
}
