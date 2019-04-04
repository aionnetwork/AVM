package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.tooling.hash.HashUtils;
import org.aion.avm.userlib.AionBuffer;
import org.aion.kernel.*;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;


public class BlockchainTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private Address premined = avmRule.getPreminedAccount();

    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 5;

    @Test
    public void testBlockchainRuntime() {
        Address dappAddress = installJarAsDApp(avmRule.getDappBytes(BlockchainTestResource.class, new byte[0], AionBuffer.class));
        org.aion.types.Address dappAddressApi = new org.aion.types.Address(dappAddress.unwrap());

        byte[] txData = "tx_data".getBytes();

        AvmRule.ResultWrapper result = avmRule.call(premined, dappAddress, BigInteger.ONE, txData, energyLimit, energyPrice);

        Block block = avmRule.getBlock();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(dappAddress.unwrap());
        buffer.put(premined.unwrap());
        buffer.put(premined.unwrap());
        buffer.putLong(energyLimit);
        buffer.putLong(energyPrice);
        buffer.putLong(BigInteger.ONE.longValue());
        buffer.put(txData);
        buffer.putLong(block.getTimestamp());
        buffer.putLong(block.getNumber());
        buffer.putLong(block.getEnergyLimit());
        buffer.put(block.getCoinbase().toBytes());
        buffer.put(block.getDifficulty().toByteArray());
        buffer.put("value".getBytes());
        buffer.putLong(avmRule.kernel.getBalance(org.aion.types.Address.wrap(new byte[32])).longValue());
        buffer.putLong(avmRule.kernel.getTransformedCode(dappAddressApi).length);
        buffer.put(HashUtils.blake2b("blake2b-message".getBytes()));
        buffer.put(HashUtils.sha256("sha256-message".getBytes()));
        buffer.put(HashUtils.keccak256("keccak256-message".getBytes()));

        byte[] expected = Arrays.copyOfRange(buffer.array(), 0, buffer.position());
        assertArrayEquals(expected, result.getTransactionResult().getReturnData());
    }

    @Test
    public void testIncorrectParameters() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BlockchainRuntimeTestFailingResource.class);
        byte[] codeAndArgs =  new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        Address dappAddress = installJarAsDApp(codeAndArgs);
        byte[] txData = "expectFailure".getBytes();

        AvmRule.ResultWrapper result = avmRule.call(premined, dappAddress, BigInteger.ONE, txData, energyLimit, energyPrice);

        assertTrue(result.getReceiptStatus().isSuccess());
        // We expect it to handle all the exceptions and return the data we initially sent in.
        assertArrayEquals(txData, result.getTransactionResult().getReturnData());
    }

    private Address installJarAsDApp(byte[] jar) {
        AvmRule.ResultWrapper result = avmRule.deploy(premined, BigInteger.ZERO, jar, energyLimit, energyPrice);
        assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return result.getDappAddress();
    }

}
