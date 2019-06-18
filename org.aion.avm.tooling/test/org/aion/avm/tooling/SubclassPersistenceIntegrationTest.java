package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIDecoder;

import avm.Address;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests the JCL and API types which can be sub-classed.
 * Also ensures that they can be persisted and reloaded correctly.
 */
public class SubclassPersistenceIntegrationTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    @Test
    public void testUserSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_user");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_user");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testEnumSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_enum");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_enum");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testExceptionSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_exception");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_exception");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testObjectSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_object");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_object");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testRuntimeExceptionSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_runtimeException");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_runtimeException");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testThrowableSubclass() throws Exception {
        Address dapp = installTestDApp(SubclassPersistenceIntegrationTestTarget.class);
        
        // Setup the environment.
        int startValue = callStaticReturnInteger(dapp, "setup_throwable");
        
        // Re-run it to make sure that save/load worked correctly.
        int endValue = callStaticReturnInteger(dapp, "check_throwable");
        Assert.assertEquals(startValue, endValue);
    }

    @Test
    public void testFailedDeploy_Exception() throws Exception {
        failedInstall(SubclassPersistenceIntegrationTestFailException.class);
    }

    @Test
    public void testFailedDeploy_Api() throws Exception {
        failedInstall(SubclassPersistenceIntegrationTestFailApi.class);
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytes(testClass, new byte[0]);
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        return new Address(createResult.copyOfTransactionOutput().orElseThrow());
    }

    private int callStaticReturnInteger(Address dapp, String methodName) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        TransactionResult result = avmRule.call(avmRule.getPreminedAccount(), dapp, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        return new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger();
    }

    private void failedInstall(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytesWithoutOptimization(testClass, new byte[0]);

        // Deploy.
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, createResult.transactionStatus.causeOfError);
    }
}
