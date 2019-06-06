package org.aion.avm.core.exceptionwrapping;

import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmFailedException;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.MockFailureInstrumentationFactory;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.abi.ABIDecoder;

import i.OutOfEnergyException;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;


public class ExceptionWrappingIntegrationTest {

    // These tests work by throwing an exception after a certain number of calls to chargeEnergy
    // Currently, deploying the test class and userlib ABI makes around 70 and 100 calls to chargeEnergy for the 2 targets
    // Any changes that cause the number of chargeEnergy calls to change, such as changing the ABI might cause this test to fail, in which case the number needs to be updated
    private final int persistentExceptionDeploymentEnergyCalls = 30;
    private final int attackExceptionHandlingTargetDeploymentEnergyCalls = 100;
    @Test
    public void testExceptionPersistence() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(TestingKernel.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TestingTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Store the exceptions.
        int systemHash = callReturnInt(block, kernel, avm, contractAddr, "storeSystem");
        // We know that this is currently 67 but that may change in the future
        Assert.assertEquals(67, systemHash);
        byte[] user = callReturnByteArray(block, kernel, avm, contractAddr, "storeUser");
        Assert.assertEquals("MESSAGE", new String(user));
        byte[] second = callReturnByteArray(block, kernel, avm, contractAddr, "getSecond");
        Assert.assertEquals("Second message", new String(second));
        int loadSystemHash = callReturnInt(block, kernel, avm, contractAddr, "loadSystem");
        Assert.assertEquals(systemHash, loadSystemHash);
        byte[] loadUser = callReturnByteArray(block, kernel, avm, contractAddr, "loadUser");
        Assert.assertEquals("MESSAGE", new String(loadUser));
        
        avm.shutdown();
    }

    @Test
    public void testOutOfEnergy() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(persistentExceptionDeploymentEnergyCalls, () -> {throw new OutOfEnergyException();}), new EmptyCapabilities(), new AvmConfiguration());
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(TestingKernel.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TestingTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }

    @Test
    public void testNullPointerException() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(PersistentExceptionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(persistentExceptionDeploymentEnergyCalls, () -> {throw new NullPointerException();}), new EmptyCapabilities(), new AvmConfiguration());
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(TestingKernel.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TestingTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // The next call will perform 10 block enters, thus triggering our failure.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, callStaticStatus(block, kernel, avm, contractAddr, "storeSystem"));
        
        avm.shutdown();
    }

    @Test
    public void testOutOfMemoryError() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(AttackExceptionHandlingTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(attackExceptionHandlingTargetDeploymentEnergyCalls, () -> {throw new OutOfMemoryError();}), new EmptyCapabilities(), new AvmConfiguration());
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(TestingKernel.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TestingTransaction[] {create})[0].get();
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
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(AttackExceptionHandlingTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = NodeEnvironment.singleton.buildAvmInstance(new MockFailureInstrumentationFactory(attackExceptionHandlingTargetDeploymentEnergyCalls, () -> {throw new OutOfMemoryError();}), new EmptyCapabilities(), new AvmConfiguration());
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(TestingKernel.PREMINED_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new TestingTransaction[] {create})[0].get();
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


    private int callReturnInt(TestingBlock block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        byte[] result = commonSuccessCall(block, kernel, avm, contractAddr, methodName);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private byte[] callReturnByteArray(TestingBlock block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        byte[] result = commonSuccessCall(block, kernel, avm, contractAddr, methodName);
        return new ABIDecoder(result).decodeOneByteArray();
    }

    private byte[] commonSuccessCall(TestingBlock block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return result.getReturnData();
    }

    private ResultCode callStaticStatus(TestingBlock block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        TransactionResult result = commonCallStatic(block, kernel, avm, contractAddr, methodName);
        return result.getResultCode();
    }

    private TransactionResult commonCallStatic(TestingBlock block, KernelInterface kernel, AvmImpl avm, Address contractAddr, String methodName) {
        org.aion.types.Address from = TestingKernel.PREMINED_ADDRESS;
        long energyLimit = 1_000_000l;
        byte[] argData = (null != methodName)
                ? ABIUtil.encodeMethodArguments(methodName)
                : new byte[0];
        TestingTransaction call = TestingTransaction.call(from, org.aion.types.Address.wrap(contractAddr.toByteArray()), kernel.getNonce(from), BigInteger.ZERO, argData, energyLimit, 1l);
        return avm.run(kernel, new TestingTransaction[] {call})[0].get();
    }
}
