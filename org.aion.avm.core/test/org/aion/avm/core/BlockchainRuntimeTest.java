package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.HashUtils;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.aion.avm.core.util.Helpers.address;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


public class BlockchainRuntimeTest {
    // kernel & vm
    private KernelInterfaceImpl kernel;
    private Avm avm;

    private org.aion.vm.api.interfaces.Address premined = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBlockchainRuntime() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BlockchainRuntimeTestResource.class, AionBuffer.class);
        org.aion.vm.api.interfaces.Address dappAddress = installJarAsDApp(jar);
        
        org.aion.vm.api.interfaces.Address from = premined;
        org.aion.vm.api.interfaces.Address to = dappAddress;
        BigInteger value = BigInteger.ONE;
        byte[] txData = "tx_data".getBytes();
        long energyLimit = 2_000_000;
        long energyPrice = 3;

        byte[] blockPrevHash = Helpers.randomBytes(32);
        long blockNumber = 4;
        org.aion.vm.api.interfaces.Address blockCoinbase = address(5);
        long blockTimestamp = 6;
        byte[] blockData = "block_data".getBytes();

        Transaction tx = Transaction.call(from, to, kernel.getNonce(premined).longValue(), value, txData, energyLimit, energyPrice);
        Block block = new Block(blockPrevHash, blockNumber, blockCoinbase, blockTimestamp, blockData);

        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(to.toBytes());
        buffer.put(from.toBytes());
        buffer.put(from.toBytes());
        buffer.putLong(energyLimit);
        buffer.putLong(energyPrice);
        buffer.putLong(value.longValue());
        buffer.put(txData);
        buffer.putLong(blockTimestamp);
        buffer.putLong(blockNumber);
        buffer.putLong(block.getEnergyLimit());
        buffer.put(blockCoinbase.toBytes());
        buffer.put(blockPrevHash);
        buffer.put(block.getDifficulty().toByteArray());
        buffer.put("value".getBytes());
        buffer.putLong(kernel.getBalance(AvmAddress.wrap(new byte[32])).longValue());
        buffer.putLong(kernel.getCode(dappAddress).length);
        buffer.put(HashUtils.blake2b("blake2b-message".getBytes()));
        buffer.put(HashUtils.sha256("sha256-message".getBytes()));
        buffer.put(HashUtils.keccak256("keccak256-message".getBytes()));

        byte[] expected = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
        assertArrayEquals(expected, txResult.getReturnData());
    }

    @Test
    public void testIncorrectParameters() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BlockchainRuntimeTestFailingResource.class);
        org.aion.vm.api.interfaces.Address dappAddress = installJarAsDApp(jar);
        
        org.aion.vm.api.interfaces.Address from = premined;
        org.aion.vm.api.interfaces.Address to = dappAddress;
        BigInteger value = BigInteger.ONE;
        byte[] txData = "expectFailure".getBytes();
        long energyLimit = 2_000_000;
        long energyPrice = 3;

        byte[] blockPrevHash = Helpers.randomBytes(32);
        long blockNumber = 4;
        org.aion.vm.api.interfaces.Address blockCoinbase = address(5);
        long blockTimestamp = 6;
        byte[] blockData = "block_data".getBytes();

        Transaction tx = Transaction.call(from, to, kernel.getNonce(premined).longValue(), value, txData, energyLimit, energyPrice);
        Block block = new Block(blockPrevHash, blockNumber, blockCoinbase, blockTimestamp, blockData);

        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getStatusCode().isSuccess());
        // We expect it to handle all the exceptions and return the data we initially sent in.
        assertArrayEquals(txData, txResult.getReturnData());
    }

    @Test
    public void testInvalidAbiInput() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ABIFailureTestResource.class);
        org.aion.vm.api.interfaces.Address dappAddress = installJarAsDApp(jar);
        
        org.aion.vm.api.interfaces.Address from = premined;
        org.aion.vm.api.interfaces.Address to = dappAddress;
        BigInteger value = BigInteger.ONE;
        byte[] txData = "not encoded for ABI usage".getBytes();
        long energyLimit = 2_000_000;
        long energyPrice = 3;

        byte[] blockPrevHash = Helpers.randomBytes(32);
        long blockNumber = 4;
        org.aion.vm.api.interfaces.Address blockCoinbase = address(5);
        long blockTimestamp = 6;
        byte[] blockData = "block_data".getBytes();

        Transaction tx = Transaction.call(from, to, kernel.getNonce(premined).longValue(), value, txData, energyLimit, energyPrice);
        Block block = new Block(blockPrevHash, blockNumber, blockCoinbase, blockTimestamp, blockData);

        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        // Note that we are expecting AvmException, which the DAppExecutor handles as FAILED.
        Assert.assertEquals(TransactionResult.Code.FAILED, txResult.getStatusCode());
    }


    private org.aion.vm.api.interfaces.Address installJarAsDApp(byte[] jar) {
        byte[] arguments = null;
        Transaction tx = Transaction.create(premined, kernel.getNonce(premined).longValue(), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), 2_000_000L, 1L);
        TransactionContext txContext = new TransactionContextImpl(tx, new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]));
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getStatusCode().isSuccess());

        return AvmAddress.wrap(txResult.getReturnData());
    }
}
