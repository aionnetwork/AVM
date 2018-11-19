package org.aion.avm.core.exceptionwrapping;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.MockFailureInstrumentationFactory;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class ExceptionWrappingIntegrationTest {
    @Test
    public void testExceptionPersistence() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, 0L, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // Store the exceptions.
        int systemHash = ((Integer)callStatic(block, kernel, avm, contractAddr, "storeSystem")).intValue();
        // We know that this is currently 5 but that may change in the future.
        Assert.assertEquals(5, systemHash);
        byte[] user = (byte[])callStatic(block, kernel, avm, contractAddr, "storeUser");
        Assert.assertEquals("MESSAGE", new String(user));
        byte[] second = (byte[])callStatic(block, kernel, avm, contractAddr, "getSecond");
        Assert.assertEquals("Second message", new String(second));
        int loadSystemHash = ((Integer)callStatic(block, kernel, avm, contractAddr, "loadSystem")).intValue();
        Assert.assertEquals(systemHash, loadSystemHash);
        byte[] loadUser = (byte[])callStatic(block, kernel, avm, contractAddr, "loadUser");
        Assert.assertEquals("MESSAGE", new String(loadUser));
        
        avm.shutdown();
    }

    @Test
    public void testOutOfEnergy() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(10, () -> {throw new OutOfEnergyException();}), kernel);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, 0L, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(TransactionResult.Code.FAILED_OUT_OF_ENERGY, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }

    @Test
    public void testNullPointerException() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(10, () -> {throw new NullPointerException();}), kernel);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, 0L, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(TransactionResult.Code.FAILED_EXCEPTION, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }


    private Object callStatic(Block block, KernelInterface kernel,  Avm avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return TestingHelper.decodeResult(result);
    }

    private TransactionResult.Code callStaticStatus(Block block, KernelInterface kernel,  Avm avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        return result.getStatusCode();
    }

    private TransactionResult commonCallStatic(Block block, KernelInterface kernel, Avm avm, Address contractAddr, String methodName) {
        byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(from, contractAddr.unwrap(), kernel.getNonce(from), BigInteger.ZERO, argData, energyLimit, 1l);
        return avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
    }
}
