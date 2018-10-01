package org.aion.kernel;

import org.aion.avm.core.util.Helpers;

import java.util.Arrays;
import java.util.Objects;


public class Transaction {
    public static Transaction create(byte[] from, long nonce, long value, byte[] data, long energyLimit, long energyPrice) {
        return new Transaction(Type.CREATE, from, null, nonce, value, data, energyLimit, energyPrice);
    }

    public static Transaction call(byte[] from, byte[] to, long nonce, long value, byte[] data, long energyLimit, long energyPrice) {
        return new Transaction(Type.CALL, from, to, nonce, value, data, energyLimit, energyPrice);
    }

    public static Transaction garbageCollect(byte[] target, long nonce, long energyLimit, long energyPrice) {
        // This may seem a bit odd but we state that the "target" of the GC is the "sender" address.
        // This is because, on a conceptual level, the GC is "sent to itself" but also allows the nonce check to be consistent.
        return new Transaction(Type.GARBAGE_COLLECT, target, target, nonce, 0L, new byte[0], energyLimit, energyPrice);
    }

    public enum Type {
        /**
         * The CREATE is used to deploy a new DApp.
         */
        CREATE,
        /**
         * The CALL is used when sending an invocation to an existing DApp.
         */
        CALL,
        /**
         * The GARBAGE_COLLECT is a special transaction which asks that the target DApp's storage be deterministically collected.
         * Note that this is the only transaction type which will result in a negative TransactionResult.energyUsed.
         */
        GARBAGE_COLLECT,
    }

    Type type;

    byte[] from;

    byte[] to;

    long nonce;

    long value;

    byte[] data;

    long energyLimit;

    long energyPrice;

    protected Transaction(Type type, byte[] from, byte[] to, long nonce, long value, byte[] data, long energyLimit, long energyPrice) {
        Objects.requireNonNull(type, "The transaction `type` can't be NULL");
        Objects.requireNonNull(from, "The transaction `from` can't be NULL");
        if (type == Type.CREATE) {
           if (to != null) {
               throw new IllegalArgumentException("The transaction `to` has to be NULL for CREATE");
           }
        } else {
            Objects.requireNonNull(to, "The transaction `to`  can't be NULL for non-CREATE");
        }

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
                ", from=" + Helpers.bytesToHexString(Arrays.copyOf(from, 4)) +
                ", to=" + Helpers.bytesToHexString(Arrays.copyOf(to, 4)) +
                ", value=" + value +
                ", data=" + Helpers.bytesToHexString(data) +
                ", energyLimit=" + energyLimit +
                '}';
    }
}
