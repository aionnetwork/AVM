package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.avm.api.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code, when invoked within independent transactions.
 */
public class HashCodeIntegrationTest {
    private org.aion.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.kernel = new TestingKernel();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testPersistentHashCode() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HashCodeIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        byte[] expStorageRootHash = Helpers.hexStringToBytes("000000780e0e006b0c450e25740b272f41e3cf2c431120420e556e505113001d");
        Assert.assertArrayEquals(expStorageRootHash, createResult.getStorageRootHash());

        Address contractAddr = new Address(createResult.getReturnData());
        // Store an object.
        int systemHash = callStatic(block, contractAddr, "persistNewObject");
        // We know that this is the current value, but that may change in the future.
        Assert.assertEquals(115, systemHash);
        // Fetch it and verify the hashCode is loaded.
        int loadSystemHash = callStatic(block, contractAddr, "readPersistentHashCode");
        Assert.assertEquals(systemHash, loadSystemHash);
    }


    private int callStatic(Block block, Address contractAddr, String methodName) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(deployer, org.aion.types.Address.wrap(contractAddr.unwrap()), kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        // Both of the calls this test makes to this helper leave the data in the same state so we can check the hash, here.
        byte[] expStorageRootHash = Helpers.hexStringToBytes("0000000b0e0e006b0c450e25740b272f41e3cf2c431120420e556e505113006e");
        Assert.assertArrayEquals(expStorageRootHash, result.getStorageRootHash());
        ABIDecoder decoder = new ABIDecoder(result.getReturnData());
        return decoder.decodeOneInteger();
    }
}
