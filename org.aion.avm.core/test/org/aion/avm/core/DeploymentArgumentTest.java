package org.aion.avm.core;

import avm.Address;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;

import java.math.BigInteger;

import org.junit.*;


/**
 * This suite is really just for viewing the deployment costs of some of our various Dapp examples.
 * Nothing explicitly is actually verified by these 'tests'.
 * The purpose is more to give us an idea about how our deployment costs look for different Dapps.
 */
public class DeploymentArgumentTest {
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1L;
    private static final org.aion.types.Address DEPLOYER = TestingKernel.PREMINED_ADDRESS;
    private static final byte[] JAR = JarBuilder.buildJarForMainAndClassesAndUserlib(DeploymentArgumentTarget.class);
    private static final byte[] SMALL_JAR = JarBuilder.buildJarForMainAndClasses(DeploymentArgumentSmallTarget.class);

    private static TestingKernel kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(),
            System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);
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
                .encodeOneAddressArray(new Address[] {new Address(DEPLOYER.toBytes())})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        AvmTransactionResult result = deployContract(encodedArguments);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testIncorrectArguments() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneStringArray(new String[] {"string", "wrong"})
                .encodeOneAddressArray(new Address[] {new Address(DEPLOYER.toBytes())})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .toBytes();
        AvmTransactionResult result = deployContract(encodedArguments);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testMissingArguments() {
        AvmTransactionResult result = deployContract(new byte[0]);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testCorrectSubDeployment() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneString("string")
                .encodeOneAddressArray(new Address[] {new Address(DEPLOYER.toBytes())})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        AvmTransactionResult result = deployContract(encodedArguments);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        
        org.aion.types.Address target = new org.aion.types.Address(result.getReturnData());
        AvmTransactionResult callResult = callContract(target, "correctDeployment");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void testIncorrectSubDeployment() {
        byte[] encodedArguments = new ABIStreamingEncoder()
                .encodeOneString("string")
                .encodeOneAddressArray(new Address[] {new Address(DEPLOYER.toBytes())})
                .encodeOneInteger(5)
                .encodeOneDouble(6.7)
                .encodeOneByteArray(SMALL_JAR)
                .toBytes();
        AvmTransactionResult result = deployContract(encodedArguments);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        
        org.aion.types.Address target = new org.aion.types.Address(result.getReturnData());
        AvmTransactionResult callResult = callContract(target, "incorrectDeployment");
        // (note that this is still a success since the call expects to fail, internally)
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }


    private AvmTransactionResult deployContract(byte[] args) {
        byte[] payload = new CodeAndArguments(JAR, args).encodeToBytes();
        TestingTransaction create = TestingTransaction.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, payload, ENERGY_LIMIT, ENERGY_PRICE);
        return (AvmTransactionResult)avm.run(kernel, new TestingTransaction[] {create})[0].get();
    }

    private AvmTransactionResult callContract(org.aion.types.Address target, String methodName) {
        byte[] argData = new ABIStreamingEncoder().encodeOneString(methodName).toBytes();
        TestingTransaction call = TestingTransaction.call(DEPLOYER, target, kernel.getNonce(DEPLOYER), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(kernel, new TestingTransaction[] {call})[0].get();
        return result;
    }
}
