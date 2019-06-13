package org.aion.avm.tooling.bootstrapmethods;

import avm.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.kernel.AvmTransactionResult;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertTrue;

public class BootstrapsCannotBeCalledTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testStringConcatFactoryMakeConcat() {
        AvmTransactionResult result = deployContract(MakeConcatTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    @Test
    public void testStringConcatFactoryMakeConcatWithConstants() {
        AvmTransactionResult result = deployContract(MakeConcatWithConstantsTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    @Test
    public void testLambdaMetaFactory() {
        AvmTransactionResult result = deployContract(MetaFactoryTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    private AvmTransactionResult deployContract(Class<?> contract) {
        return avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytesWithoutOptimization(contract, null)).getTransactionResult();
    }

}
