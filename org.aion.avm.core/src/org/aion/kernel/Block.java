package org.aion.kernel;

import java.math.BigInteger;

import org.aion.types.Address;


public class Block {

    private byte[] prevHash;

    private long number;

    private Address coinbase;

    private long timestamp;

    private byte[] data;

    public Block(byte[] prevHash, long number, Address coinbase, long timestamp, byte[] data) {
        this.prevHash = prevHash;
        this.number = number;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.data = data;
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public long getNumber() {
        return number;
    }

    public Address getCoinbase() {
        return coinbase;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }

    // TODO: consider adding the following fields into constructor

    public long getEnergyLimit() {
        return 10_000_000L;
    }

    public BigInteger getDifficulty() {
        return BigInteger.valueOf(10_000_000L);
    }
}
