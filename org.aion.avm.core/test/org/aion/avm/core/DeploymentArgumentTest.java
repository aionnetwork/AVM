package org.aion.avm.core;

import avm.Address;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;

import java.math.BigInteger;

import org.aion.types.TransactionResult;
import org.junit.*;


/**
 * This suite is really just for viewing the deployment costs of some of our various Dapp examples.
 * Nothing explicitly is actually verified by these 'tests'.
 * The purpose is more to give us an idea about how our deployment costs look for different Dapps.
 */
public class DeploymentArgumentTest {
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1L;
    private static final AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static final Address DEPLOYER_API = new Address(DEPLOYER.toByteArray());
    private static final byte[] JAR = JarBuilder.buildJarForMainAndClassesAndUserlib(DeploymentArgumentTarget.class);
    private static final byte[] SMALL_JAR = JarBuilder.buildJarForMainAndClasses(DeploymentArgumentSmallTarget.class);

    private static TestingState kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(),
            System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testCorrectArguments() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneString("string")
                .encodeOneAddressArray(new Address[] {DEPLOYER_API})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        TransactionResult result = deployContract(encodedArguments);
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIncorrectArguments() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneStringArray(new String[] {"string", "wrong"})
                .encodeOneAddressArray(new Address[] {DEPLOYER_API})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .toBytes();
        TransactionResult result = deployContract(encodedArguments);
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
    }

    @Test
    public void testMissingArguments() {
        TransactionResult result = deployContract(new byte[0]);
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
    }

    @Test
    public void testCorrectSubDeployment() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneString("string")
                .encodeOneAddressArray(new Address[] {DEPLOYER_API})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        TransactionResult result = deployContract(encodedArguments);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        AionAddress target = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
        TransactionResult callResult = callContract(target, "correctDeployment");
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
    }

    @Test
    public void testIncorrectSubDeployment() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneString("string")
                .encodeOneAddressArray(new Address[] {DEPLOYER_API})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        TransactionResult result = deployContract(encodedArguments);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        AionAddress target = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
        TransactionResult callResult = callContract(target, "incorrectDeployment");
        // (note that this is still a success since the call expects to fail, internally)
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
    }


    private TransactionResult deployContract(byte[] args) {
        byte[] payload = new CodeAndArguments(JAR, args).encodeToBytes();
        Transaction create = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, payload, ENERGY_LIMIT, ENERGY_PRICE);
        return avm.run(kernel, new Transaction[] {create})[0].getResult();
    }

    private TransactionResult callContract(AionAddress target, String methodName) {
        byte[] argData = new ABIStreamingEncoder().encodeOneString(methodName).toBytes();
        Transaction call = AvmTransactionUtil.call(DEPLOYER, target, kernel.getNonce(DEPLOYER), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = avm.run(kernel, new Transaction[] {call})[0].getResult();
        return result;
    }
}
