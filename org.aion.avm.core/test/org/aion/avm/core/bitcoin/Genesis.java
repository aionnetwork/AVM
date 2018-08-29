package org.aion.avm.core.bitcoin;

public class Genesis extends Block {

    public Genesis(int version, byte[] hashPrevBlock, byte[] hashMerkleRoot, int time, int bits, int nonce) {
        super(version, hashPrevBlock, hashMerkleRoot, time, bits, nonce);
    }
}
