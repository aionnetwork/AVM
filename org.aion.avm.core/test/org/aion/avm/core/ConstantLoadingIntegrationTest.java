package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import avm.Address;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests that we can do things like default methods with constants in the interface, etc, to prove
 * that constants are being correctly loaded, and can be referenced from, the constant class.
 */
public class ConstantLoadingIntegrationTest {
    private org.aion.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private TestingKernel kernel;
    private AvmImpl avm;

    Block block;

    @Before
    public void setup() {
        block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testCreation() throws Exception {
        Address contractAddr = deploy();
        
        // Test just the creation modes.
        int bareHash = 59;
        int bareLength = 6;
        int populateHash = 62;
        int populateLength = 3;
        byte[] bare = callStatic(block, contractAddr, 0);
        byte[] populated = callStatic(block, contractAddr, 1);
        
        Assert.assertEquals((byte)bareHash, bare[0]);
        Assert.assertEquals((byte)bareLength, bare[1]);
        Assert.assertEquals((byte)populateHash, populated[0]);
        Assert.assertEquals((byte)populateLength, populated[1]);
    }

    @Test
    public void testPersistence() throws Exception {
        Address contractAddr = deploy();
        
        // Run the creation and then test the read calls.
        callStatic(block, contractAddr, 0);
        callStatic(block, contractAddr, 1);
        
        int bareHash = 59;
        int bareLength = 6;
        int populateHash = 62;
        int populateLength = 3;
        byte[] bare = callStatic(block, contractAddr, 2);
        byte[] populated = callStatic(block, contractAddr, 3);
        
        Assert.assertEquals((byte)bareHash, bare[0]);
        Assert.assertEquals((byte)bareLength, bare[1]);
        Assert.assertEquals((byte)populateHash, populated[0]);
        Assert.assertEquals((byte)populateLength, populated[1]);
    }


    private Address deploy() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ConstantLoadingIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        return new Address(createResult.getReturnData());
    }

    private byte[] callStatic(Block block, Address contractAddr, int code) {
        long energyLimit = 1_000_000l;
        byte[] argData = new byte[] { (byte)code };
        Transaction call = Transaction.call(deployer, org.aion.types.Address.wrap(contractAddr.toByteArray()), kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return result.getReturnData();
    }
}
