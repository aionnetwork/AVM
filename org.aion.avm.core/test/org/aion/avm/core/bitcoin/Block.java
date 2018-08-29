package org.aion.avm.core.bitcoin;

import java.math.BigInteger;

public class Block {

    // Block header fields, see https://en.bitcoin.it/wiki/Block_hashing_algorithm
    private int version;
    private byte[] hashPrevBlock;
    private byte[] hashMerkleRoot;
    private int time;
    private int bits;
    private int nonce;

    public Block(int version, byte[] hashPrevBlock, byte[] hashMerkleRoot, int time, int bits, int nonce) {
        this.version = version;
        this.hashPrevBlock = hashPrevBlock;
        this.hashMerkleRoot = hashMerkleRoot;
        this.time = time;
        this.bits = bits;
        this.nonce = nonce;
    }
    public static Block fromBytes(byte[] bytes) {
        return null;
    }

    public  byte[] toBytes() {
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public byte[] getHash() {
        return null;
    }

    public BigInteger getDifficulty() {
        // TODO: depends on difficulty adjustment rule
        return null;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getHashPrevBlock() {
        return hashPrevBlock;
    }

    public byte[] getHashMerkleRoot() {
        return hashMerkleRoot;
    }

    public int getTime() {
        return time;
    }

    public int getBits() {
        return bits;
    }

    public int getNonce() {
        return nonce;
    }
}
