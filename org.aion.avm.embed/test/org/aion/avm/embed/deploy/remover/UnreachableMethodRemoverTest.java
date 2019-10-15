package org.aion.avm.embed.deploy.remover;

import avm.Address;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.embed.deploy.remover.resources.ClassA;
import org.aion.avm.embed.deploy.remover.resources.Main;
import org.aion.avm.embed.deploy.remover.resources.ClassB;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.eliminator.UnreachableMethodRemover;
import org.aion.avm.userlib.CodeAndArguments;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class UnreachableMethodRemoverTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;

    @Test
    public void testInvokeStatic() throws Exception {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(Main.class, ClassA.class, ClassB.class);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = UnreachableMethodRemover.optimize(compiler.getJarFileBytes());
        byte[] data = new CodeAndArguments(optimizedDappBytes, null).encodeToBytes();

        AvmRule.ResultWrapper result = avmRule.deploy(sender, value, data);
        assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Address contract = result.getDappAddress();

        result = avmRule.call(sender, contract, value, new byte[0]);
        assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
    }
}
