package org.aion.avm.core.unification;

import java.math.BigInteger;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.types.Address;
import org.aion.kernel.Transaction;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * WARNING:  These tests currently fail in order to demonstrate the problem reported by issue-362.
 * TODO:  Ensure that none of these failures still count as "pass" before we close issue-362.
 */
public class CommonSuperClassTest {
    private static long ENERGY_LIMIT = 10_000_000L;
    private static long ENERGY_PRICE = 1L;
    private static Address DEPLOYER = TestingKernel.PREMINED_ADDRESS;
    private static Block BLOCK = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private static TestingKernel KERNEL = new TestingKernel(BLOCK);
    private AvmImpl avm;

    @Before
    public void setup() {
        AvmConfiguration config = new AvmConfiguration();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @After
    public void tearDown() {
        try {
            avm.shutdown();
        } catch (Throwable t) {
            // Note that combineClassAndJclInterface() leaves the AVM instance in a broken state, which will be reported here, but we aren't concerned.
        }
    }

    @Test
    public void combineClassAndInterface() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineClassAndInterface.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        Transaction deployment = Transaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment})[0].get();
        // TODO (issue-362): Change this to expect SUCCESS once we fix this bug (VerifyError):
        // -interfaces implicitly descend from shadow.Object in ParentPointers but interfaces cannot descend from a concrete type other than java.lang.Object.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, deploymentResult.getResultCode());
    }

    @Test
    public void combineClassAndJclInterface() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineClassAndJclInterface.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        Transaction deployment = Transaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }

    @Test
    public void combineOverlappingInterfacesA() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineOverlappingInterfacesA.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        Transaction deployment = Transaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment})[0].get();
        // TODO (issue-362): Change this to expect FAILED_REJECTED once we fix this bug (VerifyError):
        // -superinterface relationships are not properly consulted as common superclass
        // -this should be explicitly rejected as it contains an ambiguous type unification
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, deploymentResult.getResultCode());
    }

    @Test
    public void combineOverlappingInterfacesB() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineOverlappingInterfacesB.class, CommonSuperClassTypes.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        Transaction deployment = Transaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment})[0].get();
        // TODO (issue-362): Change this to expect FAILED_REJECTED once we fix this bug (VerifyError):
        // -superinterface relationships are not properly consulted as common superclass
        // -this should be explicitly rejected as it contains an ambiguous type unification
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, deploymentResult.getResultCode());
    }

    @Test
    public void combineWithExceptions() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CommonSuperClassTarget_combineWithExceptions.class);
        byte[] arguments = new byte[0];
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();
        
        Transaction deployment = Transaction.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult deploymentResult = avm.run(KERNEL, new Transaction[] {deployment})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, deploymentResult.getResultCode());
    }
}
