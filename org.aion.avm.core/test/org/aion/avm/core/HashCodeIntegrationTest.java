package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
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
    @Test
    public void testPersistentHashCode() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(HashCodeIntegrationTestTarget.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContextImpl(create, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // Store an object.
        int systemHash = ((Integer)callStatic(block, avm, contractAddr, "persistNewObject")).intValue();
        // We know that this is currently 3 but that may change in the future.
        Assert.assertEquals(3, systemHash);
        // Fetch it and verify the hashCode is loaded.
        int loadSystemHash = ((Integer)callStatic(block, avm, contractAddr, "readPersistentHashCode")).intValue();
        Assert.assertEquals(systemHash, loadSystemHash);
    }


    private Object callStatic(Block block, Avm avm, Address contractAddr, String methodName) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return TestingHelper.decodeResult(result);
    }
}
