package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code, when invoked within independent transactions.
 */
public class HashCodeIntegrationTest {

    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    @Test
    public void testPersistentHashCode() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(HashCodeIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = new Transaction(Transaction.Type.CREATE, deployer, null, kernel.getNonce(deployer), 0, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContextImpl(create, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Assert.assertEquals(-2100857470, createResult.getStorageRootHash());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
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
        Transaction call = new Transaction(Transaction.Type.CALL, deployer, contractAddr.unwrap(), kernel.getNonce(deployer), 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        // Both of the calls this test makes to this helper leave the data in the same state so we can check the hash, here.
        Assert.assertEquals(257970589, result.getStorageRootHash());
        return TestingHelper.decodeResult(result);
    }
}
