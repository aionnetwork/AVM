package org.aion.kernel;

import java.util.List;

/**
 * Represents a log emitted by dapp.
 */
public class Log {
    private byte[] address;
    private List<byte[]> topics;
    private byte[] data;

    public Log(byte[] address, List<byte[]> topics, byte[] data) {
        this.address = address;
        this.topics = topics;
        this.data = data;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public List<byte[]> getTopics() {
        return topics;
    }

    public void setTopics(List<byte[]> topics) {
        this.topics = topics;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Log{" +
                "address=" + address +
                ", topics=" + topics +
                ", data=" + data +
                '}';
    }
}
