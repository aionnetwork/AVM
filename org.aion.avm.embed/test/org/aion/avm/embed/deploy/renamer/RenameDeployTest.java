package org.aion.avm.embed.deploy.renamer;

import avm.Address;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.renamer.Renamer;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.embed.deploy.renamer.resources.*;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.JarBuilder;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class RenameDeployTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @Before
    public void setup() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(RenameTarget.class, Collections.emptyMap(), ClassA.class, ClassB.class, ClassC.class, InterfaceD.class, EnumElements.class);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] renamed = Renamer.rename(compiler.getJarFileBytes());
        byte[] data = new CodeAndArguments(renamed, null).encodeToBytes();

        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void testException() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testException").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testInheritance() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testInheritance").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testComparable() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testComparable").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testUserlib() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testUserlib").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testInvokeDynamics() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testInvokeDynamics").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testEnum() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("testEnum").toBytes();
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }
}
