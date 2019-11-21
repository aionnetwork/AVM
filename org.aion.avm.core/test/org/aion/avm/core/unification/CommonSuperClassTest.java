package org.aion.avm.core.unification;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Collections;

import org.aion.avm.core.*;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIToken;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.*;

/**
 * Tests that demonstrate the ability of the {@link org.aion.avm.core.TypeAwareClassWriter} to
 * resolve type unification requests. Nearly all of the interesting things to test out here are
 * checked at compile time, and so getting these tests to simply deploy the contract is enough in
 * most cases. There are some cases where the actual objects are touched and we do perform calls to
 * the contracts for extra verification.
 *
 * These tests are intended to cover all of the possible type unification questions as exhaustively
 * as possible.
 */
public class CommonSuperClassTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    private static long ENERGY_LIMIT = 10_000_000L;
    private static long ENERGY_PRICE = 1L;
    private static AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingState KERNEL;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        KERNEL = new TestingState(BLOCK);
        AvmConfiguration config = new AvmConfiguration();
        config.enableVerboseContractErrors = true;
        config.preserveDebuggability = false;
        config.enableBlockchainPrintln = REPORT;
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void combineInterfaces() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithInterfaces.class, Collections.emptyMap(), CommonSuperClassTypes.class,
            AionList.class, AionBuffer.class, ABIEncoder.class, ABIToken.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());

        // We actually call the contract and interact with the types we received.
        Transaction call = AvmTransactionUtil.call(DEPLOYER, new AionAddress(deploymentResult.copyOfTransactionOutput().orElseThrow()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(callResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineJcl() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithJcl.class, Collections.emptyMap(), CommonSuperClassTypes.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineApi() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithApi.class, Collections.emptyMap(), CommonSuperClassTypes.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineUserlib() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithUserlib.class, Collections.emptyMap(), CommonSuperClassTarget_combineWithApi.class, CommonSuperClassTypes.class, AionMap.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineEnums() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithEnums.class, Collections.emptyMap(), CommonSuperClassTypes.class, AionList.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());

        Transaction call = AvmTransactionUtil.call(DEPLOYER, new AionAddress(deploymentResult.copyOfTransactionOutput().orElseThrow()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
        assertTrue(callResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineExceptions() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithExceptions.class, Collections.emptyMap(), CommonSuperClassTypes.class, AionMap.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());

        // We actually call the contract and interact with the types we received.
        Transaction call = AvmTransactionUtil.call(DEPLOYER, new AionAddress(deploymentResult.copyOfTransactionOutput().orElseThrow()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
        assertTrue(callResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineArrays() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineWithArrays.class, Collections.emptyMap(), CommonSuperClassTypes.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineAmbiguousUserClasses() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineAmbiguousClasses.class, Collections.emptyMap(), CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());

        // We actually call the contract and interact with the types we received back from the ambiguous call.
        Transaction call = AvmTransactionUtil.call(DEPLOYER, new AionAddress(deploymentResult.copyOfTransactionOutput().orElseThrow()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
        assertTrue(callResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineAmbiguousArrays() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineAmbiguousArrays.class, Collections.emptyMap(), CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineAmbiguousEnums() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineAmbiguousEnums.class, Collections.emptyMap(), CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineAmbiguousExceptions() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineAmbiguousExceptions.class, Collections.emptyMap(), CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void combineAmbiguousJclClasses() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(CommonSuperClassTarget_combineAmbiguousJcl.class, Collections.emptyMap(), CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }


    @Test
    public void watchClassHierarchy() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(UnificationSample.class, Collections.emptyMap(), AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }

    @Test
    public void watchArrayClassHierarchy() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(UnificationArraySample.class, Collections.emptyMap(), UnificationSample.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction deployment = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        assertTrue(deploymentResult.transactionStatus.isSuccess());
    }
}
