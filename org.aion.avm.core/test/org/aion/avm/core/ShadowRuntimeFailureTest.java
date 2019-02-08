package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Verifies that shadow box type constructors cannot be called.
 */
public class ShadowRuntimeFailureTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Test
    public void testFailuresInDeployment() {
        // 0-7 are failures and 8 is a success.
        for (int i = 0; i < 8; ++i) {
            byte[] data = avmRule.getDappBytes(ShadowRuntimeFailureTarget.class, new byte[] {(byte)i});
            
            // deploy
            TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
            Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        }
        byte[] data = avmRule.getDappBytes(ShadowRuntimeFailureTarget.class, new byte[] {(byte)8});
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testFailuresInCall() {
        byte[] txData = avmRule.getDappBytes(ShadowRuntimeFailureTarget.class, new byte[0]);
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // 0-7 are failures and 8 is a success.
        for (int i = 0; i < 8; ++i) {
            byte[] data =  new byte[] {(byte)i};
            TransactionResult result  = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
            Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
       }
        byte[] data = new byte[] {(byte)8};
        TransactionResult result  = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertEquals(Boolean.valueOf(true), TestingHelper.decodeResult(result));
        
    }
}
