package org.aion.kernel;

import org.aion.types.AionAddress;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.util.Helpers;

import java.util.List;
import java.util.stream.Collectors;
import org.aion.vm.api.interfaces.IExecutionLog;

/**
 * Represents a log emitted by dapp.
 */
public class Log implements IExecutionLog {
    private final IExternalCapabilities capabilities;
    private byte[] address;
    private List<byte[]> topics;
    private byte[] data;

    public Log(IExternalCapabilities capabilities, byte[] address, List<byte[]> topics, byte[] data) {
        this.capabilities = capabilities;
        this.address = address;
        this.topics = topics;
        this.data = data;
    }

    @Override
    public AionAddress getSourceAddress() {
        return new AionAddress(this.address);
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    @Override
    public List<byte[]> getTopics() {
        return topics;
    }

    public void setTopics(List<byte[]> topics) {
        this.topics = topics;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * The kernel implementation of this method actually uses a hash function that can be set, although
     * blake2b is the default and it doesn't seem there is any exposed way of altering this. This
     * has to be confirmed, though this type will likely get pushed up into a common util place
     * where this decision will be made.
     *
     * @return A {@link BloomFilter} representing this log.
     */
    @Override
    public BloomFilter getBloomFilterForLog() {
        BloomFilter filter = BloomFilter.create(this.capabilities.blake2b(this.address));
        for (byte[] topic : this.topics) {
            filter.or(BloomFilter.create(this.capabilities.blake2b(topic)));
        }
        return filter;
    }

    /**
     * Not to be used.
     *
     * Eventually this type will get pulled out into a common space, where a reliable serialization
     * can be put in place so that each project agrees upon it.
     */
    @Override
    public byte[] getEncoded() {
        return null;
    }

    @Override
    public String toString() {
        return "Log{" +
                "address=" + Helpers.bytesToHexString(address) +
                ", topics=[" + topics.stream().map(Helpers::bytesToHexString).collect(Collectors.joining(",")) + "]" +
                ", data=" + Helpers.bytesToHexString(data) +
                '}';
    }
}
