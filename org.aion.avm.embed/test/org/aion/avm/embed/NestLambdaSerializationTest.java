package org.aion.avm.embed;

import static org.junit.Assert.assertTrue;

import avm.Address;
import java.math.BigInteger;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.TransactionResult;
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
        assertTrue(result.transactionStatus.isSuccess());
        Address contract = new Address(result.copyOfTransactionOutput().orElseThrow());

        // Create the nested lambda.
        avmRule.kernel.generateBlock();
        byte[] data = ABIUtil.encodeMethodArguments("createLambda");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());

        // Invoke the nested lambda to verify it is created properly.
        avmRule.kernel.generateBlock();
        data = ABIUtil.encodeMethodArguments("callLambda");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

}
