package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.types.RawDappModule;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.JvmError;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Roman Katerinenko
 */
public class AvmImplTest {
    private static Block block;

    @BeforeClass
    public static void setupClass() {
        block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    }

    @Test
    public void checkMainClassHasProperName() throws IOException {
        final var module = "com.example.avmstartuptest";
        final Path path = Paths.get(format("%s/%s.jar", "../examples/build", module));
        final byte[] jar = Files.readAllBytes(path);
        final RawDappModule dappModule = RawDappModule.readFromJar(jar);
        final var mainClassName = "com.example.avmstartuptest.MainClass";
        assertEquals(mainClassName, dappModule.mainClass);
        Map<String, byte[]> classes = dappModule.classes;
        assertEquals(1, classes.size());
        final var expectedSizeOfFile = 424;
        assertEquals(expectedSizeOfFile, classes.get(mainClassName).length);
    }

    @Test
    public void testJvmError() {
        // Note that we eventually need to test how this interacts with AvmImpl's contract entry-point but this at least proves
        // that the hierarchy is correctly put together.
        String result = null;
        try {
            throw new JvmError(new UnknownError("testing"));
        } catch (AvmException e) {
            result = e.getMessage();
        }
        assertEquals("java.lang.UnknownError: testing", result);
    }

    /**
     * Tests that, if we hit the energy limit, we continue to hit it on every attempt to charge for a new code block.
     */
    @Test
    public void testPersistentEnergyLimit() {
        // Set up the runtime.
        Map<String, byte[]> contractClasses = Helpers.mapIncludingHelperBytecode(Collections.emptyMap());
        IHelper helper = Helpers.instantiateHelper(NodeEnvironment.singleton.createInvocationClassLoader(contractClasses), 5L, 1);

        // Prove that we can charge 0 without issue.
        helper.externalChargeEnergy(0);
        assertEquals(5, helper.externalGetEnergyRemaining());

        // Run the test.
        int catchCount = 0;
        OutOfEnergyError error = null;
        try {
            helper.externalChargeEnergy(10);
        } catch (OutOfEnergyError e) {
            catchCount += 1;
            error = e;
        }
        // We didn't reset the state so this should still fail.
        try {
            helper.externalChargeEnergy(0);
        } catch (OutOfEnergyError e) {
            catchCount += 1;
            // And have the same exception.
            assertEquals(error, e);
        }
        assertEquals(2, catchCount);
        // Remove this helper as cleanup.
        IHelper.currentContractHelper.remove();
    }

    @Test
    public void testHelperStateRestore() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmImplTestResource.class);
        byte[] arguments = new byte[0];
        byte[] txData = Helpers.encodeCodeAndData(jar, arguments);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());

        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());

        // Account for the cost:  deployment, clinit, init call.
        // BytecodeFeeScheduler:  PROCESS + (PROCESSDATA * bytecodeSize * (1 + numberOfClasses) / 10)
        long deploymentProcessCost = 32000 + (10 * jar.length * (1 + 1) / 10);
        // BytecodeFeeScheduler:  CODEDEPOSIT * data.length;
        long deploymentStorageCost = 200 * txData.length;
        long clinitCost = 188l;
        assertEquals(deploymentProcessCost + deploymentStorageCost + clinitCost, result1.getEnergyUsed());

        // call (1 -> 2 -> 2)
        long transaction2EnergyLimit = 1_000_000l;
        Transaction tx2 = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, contractAddr.unwrap(), transaction2EnergyLimit, energyPrice);
        TransactionResult result2 = avm.run(new TransactionContextImpl(tx2, block));
        assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        assertArrayEquals("CALL".getBytes(), result2.getReturnData());
        // Account for the cost:  (blocks in call method) + runtime.call
        long costOfBlocks = 111l + 57l + 629l;
        long costOfRuntimeCall = 111l + 57l + 116l;
        assertEquals(costOfBlocks + costOfRuntimeCall, result2.getEnergyUsed()); // NOTE: the numbers are not calculated, but for fee schedule change detection.

        // We assume that the IHelper has been cleaned up by this point.
        assertNull(IHelper.currentContractHelper.get());
    }

    @Test
    public void testNullReturnCrossCall() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ReentrantCrossCallResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Call the callSelfForNull entry-point and it should return null to us.
        byte[] argData = ABIEncoder.encodeMethodArguments("callSelfForNull");
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, energyLimit, energyPrice);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        Object resultObject = TestingHelper.decodeResult(result);
        assertNull(resultObject);
    }

    @Test
    public void testRecursiveHashCode() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ReentrantCrossCallResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Try a few invocations of different depths, bearing in mind the change of nextHashCode between each invocation.
        // We will do 2 zero-depth calls to see the delta between the 2 calls.
        // Then, we will do an indirect call and verify that the delta is greater.
        // If the hashcode wasn't restored across reentrant calls, this wouldn't be greater as it wouldn't capture the small cost of the original
        // indirect call (since we create at least 1 object in that path).
        int zero0 = callRecursiveHash(avm, energyLimit, contractAddr, 0);
        int zero1 = callRecursiveHash(avm, energyLimit, contractAddr, 0);
        int zero2 = callRecursiveHash(avm, energyLimit, contractAddr, 0);
        int one0 = callRecursiveHash(avm, energyLimit, contractAddr, 1);
        int one1 = callRecursiveHash(avm, energyLimit, contractAddr, 1);
        int one2 = callRecursiveHash(avm, energyLimit, contractAddr, 1);
        
        assertEquals(zero1 - zero0, zero2 - zero1);
        assertEquals(one1 - one0, one2 - one1);
        assertTrue((one1 - one0) > (zero1 - zero0));
    }

    /**
     * Tests that reentrant calls do have detectable side-effects within the caller's space, when they commit.
     */
    @Test
    public void testCommitReentrantCalls() {
        boolean shouldFail = false;
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ReentrantCrossCallResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Get direct increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(avm, contractAddr, "getDirect", shouldFail));
        
        // Get near increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
        
        // Get far increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(avm, contractAddr, "getFar", shouldFail));
        
        // Get near increments from 2 to 3 and returns 3.
        assertEquals(3, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
    }

    /**
     * Tests that reentrant calls do NOT have detectable side-effects within the caller's space, when they rollback.
     */
    @Test
    public void testRollbackReentrantCalls() {
        boolean shouldFail = true;
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ReentrantCrossCallResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // We expect these to all fail, so they should be left with the initial values:  1.
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getDirect", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getFar", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
    }

    /**
     * Tests that reentrant calls do not accidentally write-back the statics, even when the caller fails.
     */
    @Test
    public void testRollbackAfterReentrantSuccess() {
        boolean shouldFail = true;
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ReentrantCrossCallResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Cause the failure.
        byte[] nearData = ABIEncoder.encodeMethodArguments("localFailAfterReentrant");
        Transaction tx = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, nearData, energyLimit, 1l);
        TransactionResult result2 = avm.run(new TransactionContextImpl(tx, block));
        assertEquals(TransactionResult.Code.FAILURE, result2.getStatusCode());
        
        // We shouldn't see any changes, since this failed.
        // We expect these to all fail, so they should be left with the initial values:  1.
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getDirect", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getFar", shouldFail));
        assertEquals(1, callReentrantAccess(avm, contractAddr, "getNear", shouldFail));
    }


    private int callRecursiveHash(Avm avm, long energyLimit, Address contractAddr, int depth) {
        byte[] argData = ABIEncoder.encodeMethodArguments("getRecursiveHashCode", depth);
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private int callReentrantAccess(Avm avm, Address contractAddr, String methodName, boolean shouldFail) {
        long energyLimit = 1_000_000l;
        byte[] nearData = ABIEncoder.encodeMethodArguments(methodName, shouldFail);
        Transaction tx = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, nearData, energyLimit, 1l);
        TransactionResult result2 = avm.run(new TransactionContextImpl(tx, block));
        assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        return ((Integer)TestingHelper.decodeResult(result2)).intValue();
    }
}
