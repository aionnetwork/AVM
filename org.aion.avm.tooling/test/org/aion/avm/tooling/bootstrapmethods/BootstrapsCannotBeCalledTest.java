package org.aion.avm.tooling.bootstrapmethods;

import avm.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertTrue;

public class BootstrapsCannotBeCalledTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testStringConcatFactoryMakeConcat() {
        TransactionResult result = deployContract(MakeConcatTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    @Test
    public void testStringConcatFactoryMakeConcatWithConstants() {
        TransactionResult result = deployContract(MakeConcatWithConstantsTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    @Test
    public void testLambdaMetaFactory() {
        TransactionResult result = deployContract(MetaFactoryTarget.class);
        assertTrue(result.getResultCode().isFailed());
    }

    private TransactionResult deployContract(Class<?> contract) {
        return avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytesWithoutOptimization(contract, null)).getTransactionResult();
    }

}
