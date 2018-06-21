package org.aion.kernel;

public class Block {

    private long number;

    private byte[] coinbase;

    private long timestamp;

    private byte[] data;

    public Block(long number, byte[] coinbase, long timestamp, byte[] data) {
        this.number = number;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getNumber() {
        return number;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return data;
    }
}
