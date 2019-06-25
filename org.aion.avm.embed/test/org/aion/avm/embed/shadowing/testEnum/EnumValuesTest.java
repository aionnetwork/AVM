package org.aion.avm.embed.shadowing.testEnum;

import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.userlib.CodeAndArguments;

import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class EnumValuesTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();


    @Test
    public void testEnumAccessForJavac() {
        byte[] clazz = TestEnumForJavacDump.generateBytecode();
        runCommonTestOnBytecode(clazz);
    }

    @Test
    public void testEnumAccessForEclipseCompiler() {
        byte[] clazz = TestEnumForEclipseCompilerDump.generateBytecode();
        runCommonTestOnBytecode(clazz);
    }


    private void runCommonTestOnBytecode(byte[] clazz) {
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put(TestEnumForValues.class.getName(), clazz);
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestResourceForValues.class, classMap);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] txData = new CodeAndArguments(compiler.getJarFileBytes(), new byte[0]).encodeToBytes();

        Address dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData).getDappAddress();

        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("testEnumAccess");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments).getDecodedReturnData();

        Assert.assertEquals(true, result);

        txDataMethodArguments = ABIUtil.encodeMethodArguments("testEnumAccessNotExist");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments);

        Assert.assertTrue(resultWrapper.getTransactionResult().transactionStatus.isFailed());
    }
}
