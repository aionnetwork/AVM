package org.aion.avm.core.unification;

import java.math.BigInteger;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIToken;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.types.Address;
import org.aion.kernel.TestingTransaction;
import org.aion.vm.api.interfaces.TransactionResult;
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
    private static long ENERGY_LIMIT = 10_000_000L;
    private static long ENERGY_PRICE = 1L;
    private static Address DEPLOYER = TestingKernel.PREMINED_ADDRESS;
    private static TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private static TestingKernel KERNEL = new TestingKernel(BLOCK);
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        AvmConfiguration config = new AvmConfiguration();
        config.enableVerboseContractErrors = true;
        config.preserveDebuggability = false;
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void combineInterfaces() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithInterfaces.class, CommonSuperClassTypes.class,
            AionList.class, AionBuffer.class, ABIEncoder.class, ABIToken.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());

        // We actually call the contract and interact with the types we received.
        TestingTransaction call = TestingTransaction.call(DEPLOYER, Address.wrap(deploymentResult.getReturnData()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new TestingTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void combineJcl() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithJcl.class, CommonSuperClassTypes.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineApi() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithApi.class, CommonSuperClassTypes.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineUserlib() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithUserlib.class, CommonSuperClassTarget_combineWithApi.class, CommonSuperClassTypes.class, AionMap.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineEnums() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithEnums.class, CommonSuperClassTypes.class, AionList.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());

        TestingTransaction call = TestingTransaction.call(DEPLOYER, Address.wrap(deploymentResult.getReturnData()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new TestingTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void combineExceptions() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithExceptions.class, CommonSuperClassTypes.class, AionMap.class, ABIException.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());

        // We actually call the contract and interact with the types we received.
        TestingTransaction call = TestingTransaction.call(DEPLOYER, Address.wrap(deploymentResult.getReturnData()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new TestingTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void combineArrays() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithArrays.class, CommonSuperClassTypes.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineAmbiguousUserClasses() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineAmbiguousClasses.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());

        // We actually call the contract and interact with the types we received back from the ambiguous call.
        TestingTransaction call = TestingTransaction.call(DEPLOYER, Address.wrap(deploymentResult.getReturnData()), KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, new byte[10], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult callResult = avm.run(KERNEL, new TestingTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void combineAmbiguousArrays() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineAmbiguousArrays.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineAmbiguousEnums() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineAmbiguousEnums.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineAmbiguousExceptions() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineAmbiguousExceptions.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineAmbiguousJclClasses() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineAmbiguousJcl.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }


    @Test
    public void watchClassHierarchy() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(UnificationSample.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void watchArrayClassHierarchy() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(UnificationArraySample.class, UnificationSample.class, AionBuffer.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        TestingTransaction deployment = TestingTransaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new TestingTransaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }
}
