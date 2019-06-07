package org.aion.avm.tooling;

import static org.junit.Assert.assertEquals;

import avm.Address;
import java.math.BigInteger;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

public class NestLambdaSerializationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testSerializeDeserializeNestedLambda() {
        byte[] jar = avmRule.getDappBytes(NestedLambdaTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        Address contract = new Address(result.getReturnData());

        // Create the nested lambda.
        avmRule.kernel.generateBlock();
        byte[] data = ABIUtil.encodeMethodArguments("createLambda");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());

        // Invoke the nested lambda to verify it is created properly.
        avmRule.kernel.generateBlock();
        data = ABIUtil.encodeMethodArguments("callLambda");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

}
