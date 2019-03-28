package org.aion.avm.tooling.shadowing.testEnum;

import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class EnumValuesTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private ABICompiler compiler = new ABICompiler();


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
        compiler.compile(jar);
        byte[] txData = new CodeAndArguments(compiler.getJarFileBytes(), new byte[0]).encodeToBytes();

        Address dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData).getDappAddress();

        byte[] txDataMethodArguments = ABIEncoder.encodeMethodArguments("testEnumAccess");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments).getDecodedReturnData();

        Assert.assertEquals(true, result);

        txDataMethodArguments = ABIEncoder.encodeMethodArguments("testEnumAccessNotExist");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments);

        Assert.assertTrue(resultWrapper.getTransactionResult().getResultCode().isFailed());
    }
}
