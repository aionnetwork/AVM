package org.aion.avm.embed.bootstrapmethods;

import avm.Address;

import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.deploy.OptimizedJarBuilder;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.types.AionAddress;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class InvokeDynamicBlockTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;

    @Test
    public void testConcat() {
        // AKI-330: debug mode is set to true deliberately to ensure number of code blocks for both
        // BlockBuildingMethodVisitor and ChargeEnergyInjectionVisitor are the same
        byte[] data = getDappBytes(true);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        Address contract = deployResult.getDappAddress();
        byte[] txData = ABIUtil.encodeMethodArguments("concat");
        String res = (String) avmRule.call(sender, contract, value, txData, 2_000_000, 1).getDecodedReturnData();
        Assert.assertEquals("AB", res);
    }

    @Test
    public void testAvmVersions() {
        byte[] data = getDappBytes(false);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        Address contract = deployResult.getDappAddress();

        avmRule.kernel.generateBlock();
        avmRule.kernel.setTransformedCode(new AionAddress(contract.toByteArray()), null);

        byte[] txData = ABIUtil.encodeMethodArguments("concat");
        String res = (String) avmRule.call(sender, contract, value, txData, 2_000_000, 1).getDecodedReturnData();
        Assert.assertEquals("AB", res);
    }

    private byte[] getDappBytes(boolean debugMode) {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(InvokeDynamicBlockTarget.class);
        byte[] arguments = ABIUtil.encodeDeploymentArguments("A", "B");
        byte[] optimizedJar = new OptimizedJarBuilder(debugMode, jar, 1)
                .withUnreachableMethodRemover()
                .withRenamer()
                .withConstantRemover()
                .getOptimizedBytes();
        return new CodeAndArguments(optimizedJar, arguments).encodeToBytes();
    }
}
