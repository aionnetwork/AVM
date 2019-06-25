package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

/**
 * As part of issue-215, we want to see if Synthetic Methods would break any of our assumptions in method
 * invocation. The Java compiler will generate two methods in the bytecode: one takes the Obejct as argument,
 * the other takes the specific type as argument. This test operates on SyntheticMethodsTestTarget to observe
 * any possible issues when we have a concrete method that overrides a generic method.
 */
public class SyntheticMethodsTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 6_000_0000;
    private static long energyPrice = 1;


    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes(SyntheticMethodsTestTarget.class, null, AionMap.class, AionSet.class, AionList.class);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testDappWorking() {
        TransactionResult result = createAndRunTransaction("getCompareResult");

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(SyntheticMethodsTestTarget.DEFAULT_VALUE, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void testCompareTo() {
        // BigInteger
        TransactionResult result1 = createAndRunTransaction("compareSomething", 1);
        Assert.assertTrue(result1.transactionStatus.isSuccess());
        TransactionResult result1Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(1, new ABIDecoder(result1Value.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        TransactionResult result2 = createAndRunTransaction("compareSomething", 2);
        Assert.assertTrue(result2.transactionStatus.isSuccess());
        TransactionResult result2Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(0, new ABIDecoder(result2Value.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        TransactionResult result3 = createAndRunTransaction("compareSomething", 3);
        Assert.assertTrue(result3.transactionStatus.isSuccess());
        TransactionResult result3Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(-1, new ABIDecoder(result3Value.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        TransactionResult result4 = createAndRunTransaction("compareSomething", 4);
        Assert.assertTrue(result4.transactionStatus.isSuccess());
        TransactionResult result4Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(100, new ABIDecoder(result4Value.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void testTarget(){
        // pick target1Impl
        TransactionResult result1 = createAndRunTransaction("pickTarget", 1);
        Assert.assertTrue(result1.transactionStatus.isSuccess());

        // check for correctness in synthetic, should get impl1 name
        TransactionResult result2 = createAndRunTransaction("getName");
        Assert.assertTrue(result2.transactionStatus.isSuccess());
        Assert.assertEquals("TargetClassImplOne", new ABIDecoder(result2.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        // pick target2Impl
        TransactionResult result3 = createAndRunTransaction("pickTarget", 2);
        Assert.assertTrue(result3.transactionStatus.isSuccess());

        // check for correctness in synthetic, should get abstract name
        TransactionResult result4 = createAndRunTransaction("getName");
        Assert.assertTrue(result4.transactionStatus.isSuccess());
        Assert.assertEquals("TargetAbstractClass", new ABIDecoder(result4.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    @Test
    public void testGenericMethodOverride(){
        int inputGeneric = 10;
        int inputOverrideGeneric = 20;

        // calling setup generics
        TransactionResult result1 = createAndRunTransaction("setGenerics",
                inputGeneric, inputOverrideGeneric);
        Assert.assertTrue(result1.transactionStatus.isSuccess());

        // retrieve each object
        TransactionResult result2 = createAndRunTransaction("getIntGen");
        Assert.assertTrue(result2.transactionStatus.isSuccess());
        Assert.assertEquals(inputGeneric, new ABIDecoder(result2.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        TransactionResult result3 = createAndRunTransaction("getIntGenSub");
        Assert.assertTrue(result3.transactionStatus.isSuccess());
        Assert.assertEquals(inputOverrideGeneric, new ABIDecoder(result3.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        TransactionResult result4 = createAndRunTransaction("getSubCopy");
        Assert.assertTrue(result4.transactionStatus.isSuccess());
        Assert.assertEquals(inputOverrideGeneric, new ABIDecoder(result4.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    private TransactionResult createAndRunTransaction(String methodName, Object ... args){
        byte[] txData = ABIUtil.encodeMethodArguments(methodName, args);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
    }
}
