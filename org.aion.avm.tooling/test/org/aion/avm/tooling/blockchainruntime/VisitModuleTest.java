package org.aion.avm.tooling.blockchainruntime;

import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;


public class VisitModuleTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;

    @BeforeClass
    public static void setUp() throws IOException {
        byte[] moduleClass = Files.readAllBytes(Paths.get("test/resources/module-info.class"));
        Assert.assertNotNull(moduleClass);

        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecodeAndUserlib(
                VisitModuleTarget.class,
                new HashMap<>(){{
                    put("module-info", moduleClass);
                }});

        dappAddr = avmRule.deploy(from, BigInteger.ZERO, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice).getDappAddress();
        Assert.assertNotNull(dappAddr);
    }

    private AvmRule.ResultWrapper call(String methodName, Object ...objects) {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments(methodName, objects);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
    }

    @Test
    public void testVisitModule() {
        AvmRule.ResultWrapper result = call("sayHello");
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
    }
}
