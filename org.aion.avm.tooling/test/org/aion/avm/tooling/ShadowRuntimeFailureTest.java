package org.aion.avm.tooling;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;

import avm.Address;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Verifies that shadow box type constructors cannot be called.
 */
public class ShadowRuntimeFailureTest {
    private static boolean preserveDebugInfo = false;

    @ClassRule
    public static AvmRule avmRule = new AvmRule(preserveDebugInfo);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testFailuresInDeployment() {
        // 0-7 are failures and 8 is a success.
        for (int i = 0; i < 8; ++i) {
            byte[] data = getDappBytesWithUserlib(ShadowRuntimeFailureTarget.class, new byte[] {(byte)i});
            
            // deploy
            TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
            Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
        }
        byte[] data = getDappBytesWithUserlib(ShadowRuntimeFailureTarget.class, new byte[] {(byte)8});
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testFailuresInCall() {
        byte[] txData = getDappBytesWithUserlib(ShadowRuntimeFailureTarget.class, new byte[0]);
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertTrue(result1.transactionStatus.isSuccess());
        Address contractAddr = new Address(result1.copyOfTransactionOutput().orElseThrow());
        
        // 0-7 are failures and 8 is a success.
        for (int i = 0; i < 8; ++i) {
            byte[] data =  new byte[] {(byte)i};
            TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
            Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
       }
        byte[] data = new byte[] {(byte)8};
        TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO, data, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(true, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());
        
    }

    private byte[] getDappBytesWithUserlib(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        JarOptimizer jarOptimizer = new JarOptimizer(preserveDebugInfo);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(mainClass, otherClasses);
        byte[] optimizedDappBytes = jarOptimizer.optimize(jar);
        return new CodeAndArguments(optimizedDappBytes, arguments).encodeToBytes();
    }
}
