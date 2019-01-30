package org.aion.avm.core.bootstrapmethods;

import static junit.framework.TestCase.assertTrue;

import java.math.BigInteger;

import org.aion.avm.core.util.AvmRule;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

public class BootstrapsCannotBeCalledTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

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
        return avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytes(contract, null)).getTransactionResult();
    }

}
