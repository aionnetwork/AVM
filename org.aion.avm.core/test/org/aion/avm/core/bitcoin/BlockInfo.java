package org.aion.avm.core.bitcoin;

import java.math.BigInteger;

public class BlockInfo {

    private Block block;
    private BigInteger totalDifficulty;

    public BlockInfo(Block block, BigInteger totalDifficulty) {
        this.block = block;
        this.totalDifficulty = totalDifficulty;
    }

    public Block getBlock() {
        return block;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }
}
