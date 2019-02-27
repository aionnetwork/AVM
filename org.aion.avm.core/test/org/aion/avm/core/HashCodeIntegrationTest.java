package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.avm.api.ABIEncoder;
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
    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private KernelInterfaceImpl kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testPersistentHashCode() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(HashCodeIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        if (KeyValueObjectGraph.USE_DELTA_HASH) {
            // Empty statics:  0xe1781 -> [0, 0, 0, 0]
            Assert.assertEquals(0xe1781, createResult.getStorageRootHash());
        } else {
            Assert.assertEquals(923521, createResult.getStorageRootHash());
        }
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Store an object.
        int systemHash = ((Integer)callStatic(block, contractAddr, "persistNewObject")).intValue();
        // We know that this is currently 3 but that may change in the future.
        Assert.assertEquals(3, systemHash);
        // Fetch it and verify the hashCode is loaded.
        int loadSystemHash = ((Integer)callStatic(block, contractAddr, "readPersistentHashCode")).intValue();
        Assert.assertEquals(systemHash, loadSystemHash);
    }


    private Object callStatic(Block block, Address contractAddr, String methodName) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        // Both of the calls this test makes to this helper leave the data in the same state so we can check the hash, here.
        if (KeyValueObjectGraph.USE_DELTA_HASH) {
            // The hash is 0 because the statics are empty, save for the reference to an empty Object, thus meaning that both have the same representation:
            // The statics contain just the identity hash of the target and the target consists only of the identity hash so they cancel out.
            Assert.assertEquals(0x0, result.getStorageRootHash());
        } else {
            Assert.assertEquals(-1723350948, result.getStorageRootHash());
        }
        return ABIDecoder.decodeOneObject(result.getReturnData());
    }
}
