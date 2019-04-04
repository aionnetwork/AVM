package org.aion.avm.core;

import avm.Address;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;

import java.math.BigInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


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

    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(),
            System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testCorrectArguments() {
        AvmTransactionResult result = deployContract("string", new Address[] {new Address(DEPLOYER.toBytes())}, (int)5, (double)6.7, SMALL_JAR);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testIncorrectArguments() {
        AvmTransactionResult result = deployContract(new String[] {"string", "wrong"}, new Address[] {new Address(DEPLOYER.toBytes())}, (int)5, (double)6.7);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testMissingArguments() {
        AvmTransactionResult result = deployContract();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testCorrectSubDeployment() {
        AvmTransactionResult result = deployContract("string", new Address[] {new Address(DEPLOYER.toBytes())}, (int)5, (double)6.7, SMALL_JAR);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        
        org.aion.types.Address target = new org.aion.types.Address(result.getReturnData());
        AvmTransactionResult callResult = callContract(target, "correctDeployment");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }

    @Test
    public void testIncorrectSubDeployment() {
        AvmTransactionResult result = deployContract("string", new Address[] {new Address(DEPLOYER.toBytes())}, (int)5, (double)6.7, SMALL_JAR);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        
        org.aion.types.Address target = new org.aion.types.Address(result.getReturnData());
        AvmTransactionResult callResult = callContract(target, "incorrectDeployment");
        // (note that this is still a success since the call expects to fail, internally)
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
    }


    private AvmTransactionResult deployContract(Object... arguments) {
        byte[] args = ABIUtil.encodeDeploymentArguments(arguments);
        byte[] payload = new CodeAndArguments(JAR, args).encodeToBytes();
        Transaction create = Transaction.create(DEPLOYER, this.kernel.getNonce(DEPLOYER), BigInteger.ZERO, payload, ENERGY_LIMIT, ENERGY_PRICE);
        return (AvmTransactionResult)this.avm.run(this.kernel, new Transaction[] {create})[0].get();
    }

    private AvmTransactionResult callContract(org.aion.types.Address target, String methodName) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(DEPLOYER, target, kernel.getNonce(DEPLOYER), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {call})[0].get();
        return result;
    }
}
