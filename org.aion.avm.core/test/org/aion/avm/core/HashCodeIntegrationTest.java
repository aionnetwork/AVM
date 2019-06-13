package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code, when invoked within independent transactions.
 */
public class HashCodeIntegrationTest {
    private AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testPersistentHashCode() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HashCodeIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        AvmTransaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = avm.run(this.kernel, new AvmTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        AionAddress contractAddr = new AionAddress(createResult.getReturnData());
        // Store an object.
        int systemHash = callStatic(contractAddr, "persistNewObject");
        // We know that this is the current value, but that may change in the future.
        Assert.assertEquals(62, systemHash);
        // Fetch it and verify the hashCode is loaded.
        int loadSystemHash = callStatic(contractAddr, "readPersistentHashCode");
        Assert.assertEquals(systemHash, loadSystemHash);
    }


    private int callStatic(AionAddress contractAddr, String methodName) {
        long energyLimit = 1_000_000l;
        byte[] argData = new ABIStreamingEncoder().encodeOneString(methodName).toBytes();
        AvmTransaction call = AvmTransactionUtil.call(deployer,contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        AvmTransactionResult result = avm.run(this.kernel, new AvmTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        ABIDecoder decoder = new ABIDecoder(result.getReturnData());
        return decoder.decodeOneInteger();
    }
}
