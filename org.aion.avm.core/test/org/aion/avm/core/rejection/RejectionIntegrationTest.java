package org.aion.avm.core.rejection;

import org.aion.avm.core.util.AvmRule;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Implemented as part of issue-305 to demonstrate how rejections are actually observed, within the transformation logic.
 */
public class RejectionIntegrationTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static final org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Test
    public void rejectNonShadowJclSubclassError() throws Exception {
        TransactionResult createResult = avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytes(RejectNonShadowJclSubclassError.class, new byte[0])).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }
}
