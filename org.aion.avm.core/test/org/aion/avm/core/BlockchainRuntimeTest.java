package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.HashUtils;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.aion.avm.core.util.Helpers.address;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class BlockchainRuntimeTest {

    // kernel & vm
    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    private byte[] premined = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddress;

    public BlockchainRuntimeTest() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BlockchainRuntimeTestResource.class);
        byte[] arguments = null;
        Transaction tx = new Transaction(Transaction.Type.CREATE, premined, null, kernel.getNonce(premined), 0, new CodeAndArguments(jar, arguments).encodeToBytes(), 1_000_000L, 1L);
        TransactionContext txContext = new TransactionContextImpl(tx, new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]));
        TransactionResult txResult = avm.run(txContext);

        dappAddress = txResult.getReturnData();
        assertTrue(txResult.getStatusCode().isSuccess());
    }

    @Test
    public void testBlockchainRuntime() {
        Transaction.Type type = Transaction.Type.CALL;
        byte[] from = premined;
        byte[] to = dappAddress;
        long value = 1;
        byte[] txData = "tx_data".getBytes();
        long energyLimit = 2_000_000;
        long energyPrice = 3;

        byte[] blockPrevHash = Helpers.randomBytes(32);
        long blockNumber = 4;
        byte[] blockCoinbase = address(5);
        long blockTimestamp = 6;
        byte[] blockData = "block_data".getBytes();

        Transaction tx = new Transaction(type, from, to, kernel.getNonce(premined), value, txData, energyLimit, energyPrice);
        Block block = new Block(blockPrevHash, blockNumber, blockCoinbase, blockTimestamp, blockData);

        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(to);
        buffer.put(from);
        buffer.put(from);
        buffer.putLong(energyLimit);
        buffer.putLong(energyPrice);
        buffer.putLong(value);
        buffer.put(txData);
        buffer.putLong(blockTimestamp);
        buffer.putLong(blockNumber);
        buffer.putLong(block.getEnergyLimit());
        buffer.put(blockCoinbase);
        buffer.put(blockPrevHash);
        buffer.put(block.getDifficulty().toByteArray());
        buffer.put("value".getBytes());
        buffer.putLong(kernel.getBalance(new byte[32]));
        buffer.putLong(kernel.getCode(dappAddress).length);
        buffer.put(HashUtils.blake2b("message".getBytes()));

        byte[] expected = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
        assertArrayEquals(expected, txResult.getReturnData());
    }
}
