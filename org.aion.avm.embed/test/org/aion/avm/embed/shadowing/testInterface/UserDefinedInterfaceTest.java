package org.aion.avm.embed.shadowing.testInterface;

import avm.Address;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class UserDefinedInterfaceTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static Address DAppAddr;

    @BeforeClass
    public static void setup() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(UserDefinedInterfaceTarget.class, OuterInterface.class, ABIEncoder.class, ABIDecoder.class, ABIException.class);
        AvmRule.ResultWrapper resultWrapper = avmRule.deploy(from, BigInteger.ZERO, new CodeAndArguments(jar, null).encodeToBytes());
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        DAppAddr = resultWrapper.getDappAddress();
    }

    /**
     * Tests initialization of generated class which holds interface fields in debug mode
     */
    @Test
    public void testInitializationInDebugMode() {
        AvmRule.ResultWrapper result = callStatic("addInt");
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(101, result.getDecodedReturnData());

        result = callStatic("concatString");
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals("abcdef", result.getDecodedReturnData());
    }

    private AvmRule.ResultWrapper callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper result = avmRule.call(from, DAppAddr, BigInteger.ZERO, data);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        return result;
    }
}
