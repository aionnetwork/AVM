package org.aion.avm.core.bitcoin;

import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;

import java.math.BigInteger;

public class Blockchain {

    private static AionMap<ByteArrayWrapper, AionList<Block>> orphanBlocks = new AionMap<>();

    private static AionMap<ByteArrayWrapper, BlockInfo> importedBlocks = new AionMap<>();

    private Genesis genesisBlock;

    private static BlockInfo latestBlock;

    public Blockchain(Genesis genesisBlock) {
        this.genesisBlock = genesisBlock;
        this.latestBlock = new BlockInfo(genesisBlock, BigInteger.ZERO);
        this.importedBlocks.put(new ByteArrayWrapper(genesisBlock.getHash()), latestBlock);
    }

    public static boolean addBlock(byte[] blockToImport) {
        // validate block
        Block block = Block.fromBytes(blockToImport);
        if (!block.isValid()) {
            return false;
        }

        // check existence
        ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());
        if (importedBlocks.containsKey(blockHash)) {
            return false;
        }

        // check if it's an orphan block
        BlockInfo parentBlockInfo = getBlockInfo(block.getHashPrevBlock());
        if (parentBlockInfo == null) {
            AionList<Block> blocks = orphanBlocks.getOrDefault(blockHash, new AionList<>());
            blocks.add(block);
            orphanBlocks.put(blockHash, blocks);
            return false;
        }

        // import the block
        BigInteger totalDifficulty = block.getDifficulty().add(parentBlockInfo.getTotalDifficulty());
        BlockInfo blockInfo = new BlockInfo(block, totalDifficulty);
        importedBlocks.put(blockHash, blockInfo);

        // re-branch
        if (totalDifficulty.compareTo(latestBlock.getTotalDifficulty()) > 0) {
            latestBlock = blockInfo;
        }

        // import orphan blocks
        AionList<Block> orphans = orphanBlocks.remove(blockHash);
        if (orphans != null) {
            for (Block b : orphans) {
                addBlock(b.toBytes()); // TODO: no need to serialize
            }
        }

        return true;
    }

    public Block getLatestBlock() {
        return latestBlock.getBlock();
    }

    public Block getBlock(long number) {
        return null;
    }

    public Block getBlock(byte[] blockHash) {
        return null;
    }

    public BigInteger getTotalDifficulty(byte[] blockHash) {
        BlockInfo blockInfo = getBlockInfo(blockHash);
        return blockInfo == null ? BigInteger.ONE.negate() : blockInfo.getTotalDifficulty();
    }

    /**
     * Verify whether a transaction has been acknowledged, and how many confirmations has been received if so.
     *
     * @param transaction The serialized transaction
     * @param blockHash The block hash
     * @param proof The merkle proof
     * @return
     */
    public int getNumberOfConfirmations(byte[] transaction, byte[] blockHash, byte[] proof) {
        return 0;
    }

    private static BlockInfo getBlockInfo(byte[] hash) {
        return importedBlocks.get(new ByteArrayWrapper(hash));
    }
}
