package org.aion.kernel;

public class Block {

    private byte[] coinbase;

    private long timestamp;

    private byte[] data;

    public Block(byte[] coinbase, long timestamp, byte[] data) {
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.data = data;
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
