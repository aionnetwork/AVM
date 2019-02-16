package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.AvmFailedException;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.MockFailureInstrumentationFactory;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;


public class ExceptionWrappingIntegrationTest {
    @Test
    public void testExceptionPersistence() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        AvmImpl avm = CommonAvmFactory.buildAvmInstance();
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Store the exceptions.
        int systemHash = ((Integer)callStatic(block, kernel, avm, contractAddr, "storeSystem")).intValue();
        // We know that this is currently 4 but that may change in the future (was 5 when this was an instance call, for example).
        Assert.assertEquals(4, systemHash);
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
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(10, () -> {throw new OutOfEnergyException();}), false);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }

    @Test
    public void testNullPointerException() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(10, () -> {throw new NullPointerException();}), false);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }

    @Test
    public void testOutOfMemoryError() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AttackExceptionHandlingTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(100, () -> {throw new OutOfMemoryError();}), false);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will spin in a loop, thus triggering our failure.
        // (we expect this failure to happen when we try to get() the response from the future).
        boolean didFail = false;
        try {
            callStaticStatus(block, kernel, avm, contractAddr, null);
        } catch (AvmFailedException e) {
            // Expected.
            didFail = true;
        }
        Assert.assertTrue(didFail);
        
        // The shutdown will actually perform the shutdown but will throw the exception, afterward (since it wants to ensure that it was observed).
        didFail = false;
        try {
            avm.shutdown();
        } catch (AvmFailedException e) {
            // Expected.
            didFail = true;
        }
        Assert.assertTrue(didFail);
    }

    @Test
    public void testOutOfMemoryErrorReentrant() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AttackExceptionHandlingTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new KernelInterfaceImpl();
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(200, () -> {throw new OutOfMemoryError();}), false);
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will spin in a loop, thus triggering our failure.
        // (we expect this failure to happen when we try to get() the response from the future).
        boolean didFail = false;
        try {
            callStaticStatus(block, kernel, avm, contractAddr, "");
        } catch (AvmFailedException e) {
            // Expected.
            didFail = true;
        }
        Assert.assertTrue(didFail);
        
        // The shutdown will actually perform the shutdown but will throw the exception, afterward (since it wants to ensure that it was observed).
        didFail = false;
        try {
            avm.shutdown();
        } catch (AvmFailedException e) {
            // Expected.
            didFail = true;
        }
        Assert.assertTrue(didFail);
    }


    private Object callStatic(Block block, KernelInterface kernel,  AvmImpl avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ABIDecoder.decodeOneObject(result.getReturnData());
    }

    private ResultCode callStaticStatus(Block block, KernelInterface kernel,  AvmImpl avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        return result.getResultCode();
    }

    private TransactionResult commonCallStatic(Block block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
        long energyLimit = 1_000_000l;
        byte[] argData = (null != methodName)
                ? ABIEncoder.encodeMethodArguments(methodName)
                : new byte[0];
        Transaction call = Transaction.call(from, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(from), BigInteger.ZERO, argData, energyLimit, 1l);
        return avm.run(kernel, new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
    }
}
