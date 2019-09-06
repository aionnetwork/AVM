package org.aion.avm.core;

import avm.Address;
import java.math.BigInteger;

import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.types.RawDappModule;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

import i.AvmThrowable;
import i.CommonInstrumentation;
import i.Helper;
import i.IInstrumentation;
import i.IRuntimeSetup;
import i.InstrumentationHelpers;
import i.JvmError;
import i.OutOfEnergyException;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import legacy_examples.avmstartuptest.MainClass;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class AvmImplTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingBlock block;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    }

    @Test
    public void testStateUpdates() {
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress from = deployer;
        AionAddress to = new AionAddress(new byte[32]);
        BigInteger value = BigInteger.valueOf(1000L);
        byte[] data = "data".getBytes();
        long energyLimit = 50_000L;
        long energyPrice = 1L;
        Transaction tx = AvmTransactionUtil.call(from, to, kernel.getNonce(from), value, data, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[] { tx }, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        // verify results
        assertTrue(result.transactionStatus.isSuccess());
        assertFalse(result.copyOfTransactionOutput().isPresent());
        assertEquals(BillingRules.getBasicTransactionCost(tx.copyOfTransactionData()), result.energyUsed);
        assertEquals(0, result.logs.size());
        assertEquals(0, result.internalTransactions.size());

        // verify state change
        assertEquals(1, kernel.getNonce(from).intValue());
        assertEquals(TestingState.PREMINED_AMOUNT.subtract(value).subtract(BigInteger.valueOf(BillingRules.getBasicTransactionCost(tx.copyOfTransactionData()) * energyPrice)), kernel.getBalance(deployer));
        assertEquals(0, kernel.getNonce(to).intValue());
        assertEquals(value, kernel.getBalance(to));
        avm.shutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransactionWithoutSignBytes() {
        TestingState kernel = new TestingState(block);

        AionAddress from = deployer;
        AionAddress to = new AionAddress(new byte[32]);
        // large value that caused issues when not specifically interpreted as a positive integer
        BigInteger value = BigInteger.valueOf(13).multiply(BigInteger.TEN.pow(18));
        byte[] data = new byte[0];
        long energyLimit = 50_000L;
        long energyPrice = 1L;

        // Verifies that we cannot create a transaction with a negative nonce/value.
        AvmTransactionUtil.call(from, to, new BigInteger(omitSignByte(kernel.getNonce(from).toByteArray())), new BigInteger(omitSignByte(value.toByteArray())), data, energyLimit, energyPrice);
    }

    /** Omits sign indication byte. Used by the Aion kernel. */
    public static byte[] omitSignByte(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length != 1 && data[0] == 0) {
            byte[] tmp = new byte[data.length - 1];
            System.arraycopy(data, 1, tmp, 0, tmp.length);
            data = tmp;
        }
        return data;
    }

    @Test
    public void checkMainClassHasProperName() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(MainClass.class);
        final RawDappModule dappModule = RawDappModule.readFromJar(jar, false, true);
        final String mainClassName = MainClass.class.getName();
        assertEquals(mainClassName, dappModule.mainClass);
        Map<String, byte[]> classes = dappModule.classes;
        assertEquals(1, classes.size());
    }

    @Test
    public void testJvmError() {
        // Note that we eventually need to test how this interacts with AvmImpl's contract entry-point but this at least proves
        // that the hierarchy is correctly put together.
        String result = null;
        try {
            throw new JvmError(new UnknownError("testing"));
        } catch (AvmThrowable e) {
            result = e.getMessage();
        }
        assertEquals("java.lang.UnknownError: testing", result);
    }

    /**
     * Tests that, if we hit the energy limit, we continue to hit it on every attempt to charge for a new code block.
     */
    @Test
    public void testPersistentEnergyLimit() {
        // Set up the runtime (note that we need to initialize the NodeEnvironment before we attach to the thread)..
        Map<String, byte[]> contractClasses = Helpers.mapIncludingHelperBytecode(Collections.emptyMap(), Helpers.loadDefaultHelperBytecode());
        AvmClassLoader avmClassLoader = NodeEnvironment.singleton.createInvocationClassLoader(contractClasses);
        
        IRuntimeSetup runtimeSetup = new Helper();
        IInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        InstrumentationHelpers.pushNewStackFrame(runtimeSetup, avmClassLoader, 5L, 1, null);

        // Prove that we can charge 0 without issue.
        instrumentation.chargeEnergy(0);
        assertEquals(5, instrumentation.energyLeft());

        // Run the test.
        int catchCount = 0;
        OutOfEnergyException error = null;
        try {
            instrumentation.chargeEnergy(10);
        } catch (OutOfEnergyException e) {
            catchCount += 1;
            error = e;
        }
        // We didn't reset the state so this should still fail.
        try {
            instrumentation.chargeEnergy(0);
        } catch (OutOfEnergyException e) {
            catchCount += 1;
            // And have the same exception.
            assertEquals(error, e);
        }
        assertEquals(2, catchCount);
        InstrumentationHelpers.popExistingStackFrame(runtimeSetup);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void testHelperStateRestore() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmImplTestResource.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(kernel, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result1.transactionStatus.isSuccess());

        AionAddress contractAddr = new AionAddress(result1.copyOfTransactionOutput().orElseThrow());

        // Account for the cost:  deployment, clinit, init call.
        long basicCost = BillingRules.getBasicTransactionCost(txData);
        long codeInstantiationOfDeploymentFee = BillingRules.getDeploymentFee(1, jar.length);
        // Note that the only <clinit> is in the generated constants class, which is free.
        long clinitCost = 0L;
        long initialStorageCost = 222;
        long transactionCost = basicCost + codeInstantiationOfDeploymentFee + clinitCost + initialStorageCost;
        assertEquals(transactionCost, result1.energyUsed);

        // call (1 -> 2 -> 2)
        long transaction2EnergyLimit = 1_000_000l;
        Transaction tx2 = AvmTransactionUtil.call(deployer, contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, contractAddr.toByteArray(), transaction2EnergyLimit, energyPrice);
        TransactionResult result2 = avm.run(kernel, new Transaction[] {tx2}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result2.transactionStatus.isSuccess());
        assertArrayEquals("CALL".getBytes(), result2.copyOfTransactionOutput().orElseThrow());
        // Account for the cost:  (blocks in call method) + runtime.call
        //code block cost for initial call
        long costOfBlocks = 51l + 31l + 321l;
        //reentrant call cost including code block cost
        long costOfRuntimeCall = 51l + 31l + 61l + (100 + 630);
        // All persistence load/store cost (note that this is a reentrant call): (2 reads at 74, 2 writes at 222)
        long runStorageCost = 74 + 74 + 222 + 222;
        // runtime cost of the initial call
        long runtimeCost = 100 + 100 + 600 + 100 + 100 + 5000 + 620;
        transactionCost = runtimeCost + BillingRules.getBasicTransactionCost(tx2.copyOfTransactionData()) + costOfBlocks + costOfRuntimeCall + runStorageCost;
        assertEquals(transactionCost, result2.energyUsed); // NOTE: the numbers are not calculated, but for fee schedule change detection.

        avm.shutdown();
    }

    @Test
    public void testNullReturnCrossCall() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // Call the callSelfForNull entry-point and it should return null to us.
        byte[] argData = encodeNoArgCall("callSelfForNull");
        callDAppVoid(kernel, avm, contractAddr, argData);
        avm.shutdown();
    }

    @Test
    public void testRecursiveHashCode() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        long energyLimit = 10_000_000l;
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // Try a few invocations of different depths, bearing in mind the change of nextHashCode between each invocation.
        // We will do 2 zero-depth calls to see the delta between the 2 calls.
        // Then, we will do an indirect call and verify that the delta is greater.
        // If the hashcode wasn't restored across reentrant calls, this wouldn't be greater as it wouldn't capture the small cost of the original
        // indirect call (since we create at least 1 object in that path).
        int zero0 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 0);
        int zero1 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 0);
        int zero2 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 0);
        int one0 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 1);
        int one1 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 1);
        int one2 = callRecursiveHash(kernel, avm, energyLimit, contractAddr, 1);
        
        assertEquals(zero1 - zero0, zero2 - zero1);
        assertEquals(one1 - one0, one2 - one1);
        assertTrue((one1 - one0) > (zero1 - zero0));
        avm.shutdown();
    }

    /**
     * Tests that reentrant calls do have detectable side-effects within the caller's space, when they commit.
     */
    @Test
    public void testCommitReentrantCalls() {
        boolean shouldFail = false;
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // Get direct increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(kernel, avm, contractAddr, "getDirect", shouldFail));
        
        // Get near increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        
        // Get far increments from 1 to 2 and returns 2.
        assertEquals(2, callReentrantAccess(kernel, avm, contractAddr, "getFar", shouldFail));
        
        // Get near increments from 2 to 3 and returns 3.
        assertEquals(3, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        avm.shutdown();
    }

    /**
     * Tests that reentrant calls do NOT have detectable side-effects within the caller's space, when they rollback.
     */
    @Test
    public void testRollbackReentrantCalls() {
        boolean shouldFail = true;
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // We expect these to all fail, so they should be left with the initial values:  1.
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getDirect", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getFar", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        avm.shutdown();
    }

    /**
     * Tests that reentrant calls do not accidentally write-back the statics, even when the caller fails.
     */
    @Test
    public void testRollbackAfterReentrantSuccess() {
        boolean shouldFail = true;
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        long energyLimit = 1_000_000l;
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // Cause the failure.
        byte[] nearData = encodeNoArgCall("localFailAfterReentrant");
        Transaction tx = AvmTransactionUtil.call(deployer, contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, nearData, energyLimit, 1L);
        TransactionResult result2 = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertEquals(AvmInternalError.FAILED_OUT_OF_ENERGY.error, result2.transactionStatus.causeOfError);

        // We shouldn't see any changes, since this failed.
        // We expect these to all fail, so they should be left with the initial values:  1.
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getDirect", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getFar", shouldFail));
        assertEquals(1, callReentrantAccess(kernel, avm, contractAddr, "getNear", shouldFail));
        avm.shutdown();
    }

    /**
     * Tests that reentrant calls do not leave any side-effects within the caller's space when the rollback only during the last part of write-back.
     */
    @Test
    public void testReentrantRollbackDuringCommit() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // We just want to call our special getFar helper with a constrained energy.
        // WARNING:  This test is very sensitive to storage billing configuration so the energy limit likely needs to be updated when that changes.
        // The write-back of the callee attempts to write statics and 2 instances.  We want it to fail at 1 instance (14_400L seems to do this).
        long failingLimit = 14_400L;
        byte[] callData = encodeCallLong("getFarWithEnergy", failingLimit);
        boolean result = callDAppBoolean(kernel, avm, contractAddr, callData);

        
        // This returns false since the value didn't change,
        assertEquals(false, result);
        avm.shutdown();
    }

    /**
     * Tests that inner classes are correctly handled by reentrant calls.
     * Also ensures that instances reachable from statics are correctly handled to arbitrary recursive depth.
     */
    @Test
    public void testReentrantRecursiveNested() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);
        
        // We just want to call our special getFar helper with a constrained energy.
        // WARNING:  This test is very sensitive to storage billing configuration so the energy limit likely needs to be updated when that changes.
        // The write-back of the callee attempts to write statics and 2 instances.  We want it to fail at 1 instance (20_000L seems to do this).
        byte[] callData = encodeCallIntInt("recursiveChangeNested", 0, 5);
        int result = callDAppInteger(kernel, avm, contractAddr, callData);
        
        // We don't want to depend on a specific hashcode (appears to be 19) but just the idea that it needs to be non-zero.
        assertTrue(0 != result);

        avm.shutdown();
    }

    /**
    * Tests that the internal call depth limit is in effect; aka, "CallDepthLimitExceededException"
    * is thrown once the limit is reached.
    */
    @Test
    public void testCallDepthLimit() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // deploy
        AionAddress contractAddr = createDApp(kernel, avm, txData);

        // Verify the internal call depth limit is in effect.
        byte[] callData = encodeCallIntInt("recursiveChangeNested", 0, 10);
        Transaction tx = AvmTransactionUtil.call(deployer, contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, callData, 20_000_000l, 1L);
        TransactionResult result2 = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result2.transactionStatus.causeOfError);

        avm.shutdown();
    }

    /**
     * Tests that a DApp can CREATE and then CALL another instance.
     */
    @Test
    public void testCreateAndCallSubApp() {
        byte incrementBy = 2;
        byte[] incrementorJar = JarBuilder.buildJarForMainAndClassesAndUserlib(IncrementorDApp.class);
        byte[] incrementorCreateData = new CodeAndArguments(incrementorJar, new byte[] {incrementBy}).encodeToBytes();
        byte[] spawnerJar = JarBuilder.buildJarForMainAndClassesAndUserlib(SpawnerDApp.class);
        byte[] spanerCreateData = new CodeAndArguments(spawnerJar, incrementorCreateData).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // CREATE the spawner.
        AionAddress spawnerAddress = createDApp(kernel, avm, spanerCreateData);
        
        // CALL to create and invoke the incrementor.
        byte[] input = new byte[] {1,2,3,4,5};
        byte[] incrementorCallData = encodeCallByteArray("incrementArray", input);
        byte[] spawnerCallData = encodeCallByteArray("spawnAndCall", incrementorCallData);
        byte[] incrementorResult = callDAppByteArray(kernel, avm, spawnerAddress, spawnerCallData);
        // We double-encoded the arguments, so double-decode the response.
        byte[] spawnerResult = new ABIDecoder(incrementorResult).decodeOneByteArray();
        assertEquals(input.length, spawnerResult.length);
        for (int i = 0; i < input.length; ++i) {
            assertEquals(incrementBy + input[i], spawnerResult[i]);
        }
        avm.shutdown();
    }

    /**
     * Tests that a DApp can CREATE for us.
     */
    @Test
    public void testCreateSubAppCall() {
        byte incrementBy = 3;
        byte[] incrementorJar = JarBuilder.buildJarForMainAndClassesAndUserlib(IncrementorDApp.class);
        byte[] incrementorCreateData = new CodeAndArguments(incrementorJar, new byte[] {incrementBy}).encodeToBytes();
        byte[] spawnerJar = JarBuilder.buildJarForMainAndClassesAndUserlib(SpawnerDApp.class);
        byte[] spanerCreateData = new CodeAndArguments(spawnerJar, incrementorCreateData).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // CREATE the spawner.
        AionAddress spawnerAddress = createDApp(kernel, avm, spanerCreateData);
        
        // CALL to create and invoke the incrementor.
        boolean shouldFail = false;
        byte[] spawnerCallData = encodeCallBool("spawnOnly", shouldFail);
        Address incrementorAddress = callDAppAddress(kernel, avm, spawnerAddress, spawnerCallData);
        
        // Call the incrementor, directly.
        byte[] input = new byte[] {1,2,3,4,5};
        byte[] incrementorCallData = encodeCallByteArray("incrementArray", input);
        
        byte[] incrementorResult = callDAppByteArray(kernel, avm, new AionAddress(incrementorAddress.toByteArray()), incrementorCallData);
        assertEquals(input.length, incrementorResult.length);
        for (int i = 0; i < input.length; ++i) {
            assertEquals(incrementBy + input[i], incrementorResult[i]);
        }
        avm.shutdown();
    }

    /**
     * Tests that a DApp can CREATE for us (but is reverted on failure).
     */
    @Test
    public void testCreateSubAppCallFailure() {
        byte incrementBy = 3;
        byte[] incrementorJar = JarBuilder.buildJarForMainAndClassesAndUserlib(IncrementorDApp.class);
        byte[] incrementorCreateData = new CodeAndArguments(incrementorJar, new byte[] {incrementBy}).encodeToBytes();
        byte[] spawnerJar = JarBuilder.buildJarForMainAndClassesAndUserlib(SpawnerDApp.class);
        byte[] spanerCreateData = new CodeAndArguments(spawnerJar, incrementorCreateData).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // CREATE the spawner.
        AionAddress spawnerAddress = createDApp(kernel, avm, spanerCreateData);
        
        // CALL to create and invoke the incrementor.
        boolean shouldFail = true;
        byte[] spawnerCallData = encodeCallBool("spawnOnly", shouldFail);
        long energyLimit = 1_000_000l;
        Transaction tx = AvmTransactionUtil.call(TestingState.PREMINED_ADDRESS, spawnerAddress, kernel.getNonce(deployer), BigInteger.ZERO, spawnerCallData, energyLimit, 1L);
        TransactionResult result2 = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertEquals(AvmInternalError.FAILED_INVALID.error, result2.transactionStatus.causeOfError);
        avm.shutdown();
    }

    /**
     * Tests that a DApp can CREATE for us, backed by a directory.
     * (same as "testCreateSubAppCall" but uses a directory backing-store).
     */
    @Test
    public void testCreateSubAppCallOnDirectory() throws Exception {
        File directory = folder.newFolder();
        byte incrementBy = 3;
        byte[] incrementorJar = JarBuilder.buildJarForMainAndClassesAndUserlib(IncrementorDApp.class);
        byte[] incrementorCreateData = new CodeAndArguments(incrementorJar, new byte[] {incrementBy}).encodeToBytes();
        byte[] spawnerJar = JarBuilder.buildJarForMainAndClassesAndUserlib(SpawnerDApp.class);
        byte[] spanerCreateData = new CodeAndArguments(spawnerJar, incrementorCreateData).encodeToBytes();
        TestingState kernel = new TestingState(directory, block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // We always start out with the PREMINE account, but that should be the only one.
        assertEquals(1, directory.listFiles().length);
        
        // CREATE the spawner (meaning another account). Expect 3 accounts because: deployer, contract, coinbase
        AionAddress spawnerAddress = createDApp(kernel, avm, spanerCreateData);
        assertEquals(3, directory.listFiles().length);
        
        // CALL to create and invoke the incrementor. Expect 4 accounts because: deployer, contract1, contract2, coinbase
        boolean shouldFail = false;
        byte[] spawnerCallData = encodeCallBool("spawnOnly", shouldFail);
        Address incrementorAddress = callDAppAddress(kernel, avm, spawnerAddress, spawnerCallData);
        assertEquals(4, directory.listFiles().length);
        
        // Restart the AVM.
        avm.shutdown();
        kernel = new TestingState(directory, block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // Call the incrementor, directly.
        byte[] input = new byte[] {1,2,3,4,5};
        byte[] incrementorCallData = encodeCallByteArray("incrementArray", input);
        
        byte[] incrementorResult = callDAppByteArray(kernel, avm, new AionAddress(incrementorAddress.toByteArray()), incrementorCallData);
        assertEquals(input.length, incrementorResult.length);
        for (int i = 0; i < input.length; ++i) {
            assertEquals(incrementBy + input[i], incrementorResult[i]);
        }
        
        // Check the state of the directory we are using to back this (4 accounts, 2 with code and 2 with only a balance).
        int codeCount = 0;
        int balanceCount = 0;
        for (File top : directory.listFiles()) {
            for (File account : top.listFiles()) {
                if ("code".equals(account.getName())) {
                    codeCount += 1;
                } else if ("balance".equals(account.getName())) {
                    balanceCount += 1;
                }
            }
        }
        assertEquals(2, codeCount);
        assertEquals(4, balanceCount);
        avm.shutdown();
    }

    @Test
    public void testDeployFailedWithNullMainClass() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(null, MainClass.class);
        deployInvalidJar(jar);
    }

    @Test
    public void testDeployFailedWithMissingMainClass() {
        byte[] jar = JarBuilder.buildJarForExplicitMainAndClasses("NonExistentClass", MainClass.class);
        deployInvalidJar(jar);
    }

    @Test
    public void testDeployFailedWithInvalidMainClass() {
        byte[] jar = JarBuilder.buildJarForExplicitMainAndClasses(".Invalid..Class.....Name", MainClass.class);
        deployInvalidJar(jar);
    }

    @Test
    public void testDeployFailedWithMissingMainMethod() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(InterfaceTestResource.class, MainClass.class);
        deployInvalidJar(jar);
    }

    @Test
    public void testDeployFailedWithInvalidMainClassBytecode() {
        byte[] jar = JarBuilder.buildJarForExplicitClassNameAndBytecode("NotAValidClass", new byte[] {0x1, 0x2, 0x3});
        deployInvalidJar(jar);
    }

    /**
     * Tests that a DApp can CREATE from within its own CREATE, so long as we only go 10 levels down (the limit).
     */
    @Test
    public void testCreateInClinit_success() {
        byte[] spawnerCreateData = buildRecursiveCreate(10);
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        TransactionResult createResult = createDAppCanFail(kernel, avm, spawnerCreateData);
        assertTrue(createResult.transactionStatus.isSuccess());
        AionAddress spawnerAddress = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
        assertNotNull(spawnerAddress);
        avm.shutdown();
    }

    /**
     * Tests that a DApp can CREATE from within its own CREATE, and fails after 11 levels down (since 10 is the limit).
     */
    @Test
    public void testCreateInClinit_failure() {
        byte[] spawnerCreateData = buildRecursiveCreate(11);
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        TransactionResult createResult = createDAppCanFail(kernel, avm, spawnerCreateData);
        // We are ultimately failing due to the AssertionError the class triggers if the create fails.
        assertEquals(AvmInternalError.FAILED_EXCEPTION.error, createResult.transactionStatus.causeOfError);
        avm.shutdown();
    }


    private int callRecursiveHash(IExternalState externalState, AvmImpl avm, long energyLimit, AionAddress contractAddr, int depth) {
        byte[] argData = encodeCallInt("getRecursiveHashCode", depth);
        Transaction call = AvmTransactionUtil.call(deployer, contractAddr, externalState.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1L);
        TransactionResult result = avm.run(externalState, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger();
    }

    private int callReentrantAccess(IExternalState externalState, AvmImpl avm, AionAddress contractAddr, String methodName, boolean shouldFail) {
            byte[] nearData = encodeCallBool(methodName, shouldFail);
        return callDAppInteger(externalState, avm, contractAddr, nearData);
    }

    private AionAddress createDApp(IExternalState externalState, AvmImpl avm, byte[] createData) {
        TransactionResult result1 = createDAppCanFail(externalState, avm, createData);
        assertTrue(result1.transactionStatus.isSuccess());
        return new AionAddress(result1.copyOfTransactionOutput().orElseThrow());
    }

    private TransactionResult createDAppCanFail(IExternalState externalState, AvmImpl avm, byte[] createData) {
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = AvmTransactionUtil.create(deployer, externalState.getNonce(deployer), BigInteger.ZERO, createData, energyLimit, energyPrice);
        return avm.run(externalState, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
    }

    private void callDAppVoid(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(externalState, avm, dAppAddress, argData);
        assertArrayEquals(new byte[0], result);
    }

    private boolean callDAppBoolean(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(externalState, avm, dAppAddress, argData);
        return new ABIDecoder(result).decodeOneBoolean();
    }

    private int callDAppInteger(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(externalState, avm, dAppAddress, argData);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private byte[] callDAppByteArray(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(externalState, avm, dAppAddress, argData);
        return new ABIDecoder(result).decodeOneByteArray();
    }

    private Address callDAppAddress(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        byte[] result = callDAppSuccess(externalState, avm, dAppAddress, argData);
        return new ABIDecoder(result).decodeOneAddress();
    }

    private byte[] callDAppSuccess(IExternalState externalState, AvmImpl avm, AionAddress dAppAddress, byte[] argData) {
        long energyLimit = 5_000_000l;
        Transaction tx = AvmTransactionUtil.call(deployer, dAppAddress, externalState.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1L);
        TransactionResult result2 = avm.run(externalState, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
        assertTrue(result2.transactionStatus.isSuccess());
        return result2.copyOfTransactionOutput().orElseThrow();
    }

    private void deployInvalidJar(byte[] jar) {
        byte[] deployment = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, deployment, energyLimit, energyPrice);
        TransactionResult result1 = avm.run(kernel, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        avm.shutdown();
        assertEquals(AvmInternalError.FAILED_INVALID_DATA.error, result1.transactionStatus.causeOfError);
    }

    private byte[] buildRecursiveCreate(int levelsToAdd) {
        byte[] args = (levelsToAdd > 1)
                ? buildRecursiveCreate(levelsToAdd - 1)
                : new byte[0];
        byte[] recursiveSpawner = JarBuilder.buildJarForMainAndClasses(RecursiveSpawnerResource.class);
        return new CodeAndArguments(recursiveSpawner, args).encodeToBytes();
    }

    private static byte[] encodeNoArgCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }

    private static byte[] encodeCallBool(String methodName, boolean arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneBoolean(arg)
                .toBytes();
    }

    private static byte[] encodeCallByteArray(String methodName, byte[] arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneByteArray(arg)
                .toBytes();
    }

    private static byte[] encodeCallLong(String methodName, long arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneLong(arg)
                .toBytes();
    }

    private static byte[] encodeCallInt(String methodName, int arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneInteger(arg)
                .toBytes();
    }

    private static byte[] encodeCallIntInt(String methodName, int arg1, int arg2) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneInteger(arg1)
                .encodeOneInteger(arg2)
                .toBytes();
    }
}
