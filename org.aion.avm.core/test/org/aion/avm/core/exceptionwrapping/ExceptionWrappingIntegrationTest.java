package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.SuspendedHelper;
import org.aion.avm.core.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class ExceptionWrappingIntegrationTest {
    @Test
    public void testExceptionPersistence() throws Exception {
        // This test doesn't use the common IHelper from setup() so uninstall it.
        SuspendedHelper suspended = new SuspendedHelper();
        
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = new Transaction(Transaction.Type.CREATE, KernelInterfaceImpl.PREMINED_ADDRESS, new byte[32], 0, 0, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContextImpl(create, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // Store the exceptions.
        int systemHash = ((Integer)callStatic(block, avm, contractAddr, "storeSystem")).intValue();
        // We know that this is currently 5 but that may change in the future.
        Assert.assertEquals(5, systemHash);
        byte[] user = (byte[])callStatic(block, avm, contractAddr, "storeUser");
        Assert.assertEquals("MESSAGE", new String(user));
        byte[] second = (byte[])callStatic(block, avm, contractAddr, "getSecond");
        Assert.assertEquals("Second message", new String(second));
        int loadSystemHash = ((Integer)callStatic(block, avm, contractAddr, "loadSystem")).intValue();
        Assert.assertEquals(systemHash, loadSystemHash);
        byte[] loadUser = (byte[])callStatic(block, avm, contractAddr, "loadUser");
        Assert.assertEquals("MESSAGE", new String(loadUser));
        
        suspended.resume();
    }

    private Object callStatic(Block block, Avm avm, Address contractAddr, String methodName) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = new Transaction(Transaction.Type.CALL, KernelInterfaceImpl.PREMINED_ADDRESS, contractAddr.unwrap(), 0, 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return TestingHelper.decodeResult(result);
    }
}
