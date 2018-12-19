package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * As part of issue-215, we want to see if Synthetic Methods would break any of our assumptions in method
 * invocation. The Java compiler will generate two methods in the bytecode: one takes the Obejct as argument,
 * the other takes the specific type as argument. This test operates on SyntheticMethodsTestTarget to observe
 * any possible issues when we have a concrete method that overrides a generic method.
 */
public class SyntheticMethodsTest {
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClasses(SyntheticMethodsTestTarget.class
                , AionMap.class
                , AionSet.class
                , AionList.class);

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testDappWorking() {
        AvmTransactionResult result = createAndRunTransaction("getCompareResult");

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertEquals(SyntheticMethodsTestTarget.DEFAULT_VALUE, TestingHelper.decodeResult(result));
    }

    @Test
    public void testCompareTo() {
        // BigInteger
        AvmTransactionResult result1 = createAndRunTransaction("compareSomething", 1);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        AvmTransactionResult result1Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(1, TestingHelper.decodeResult(result1Value));

        AvmTransactionResult result2 = createAndRunTransaction("compareSomething", 2);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result2.getResultCode());
        AvmTransactionResult result2Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(0, TestingHelper.decodeResult(result2Value));

        AvmTransactionResult result3 = createAndRunTransaction("compareSomething", 3);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result3.getResultCode());
        AvmTransactionResult result3Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(-1, TestingHelper.decodeResult(result3Value));

        AvmTransactionResult result4 = createAndRunTransaction("compareSomething", 4);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result4.getResultCode());
        AvmTransactionResult result4Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(100, TestingHelper.decodeResult(result4Value));
    }

    @Test
    public void testTarget(){
        // pick target1Impl
        AvmTransactionResult result1 = createAndRunTransaction("pickTarget", 1);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());

        // check for correctness in synthetic, should get impl1 name
        AvmTransactionResult result2 = createAndRunTransaction("getName");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result2.getResultCode());
        Assert.assertEquals("TargetClassImplOne", TestingHelper.decodeResult(result2));

        // pick target2Impl
        AvmTransactionResult result3 = createAndRunTransaction("pickTarget", 2);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result3.getResultCode());

        // check for correctness in synthetic, should get abstract name
        AvmTransactionResult result4 = createAndRunTransaction("getName");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result4.getResultCode());
        Assert.assertEquals("TargetAbstractClass", TestingHelper.decodeResult(result4));
    }

    @Test
    public void testGenericMethodOverride(){
        int inputGeneric = 10;
        int inputOverrideGeneric = 20;

        // calling setup generics
        AvmTransactionResult result1 = createAndRunTransaction("setGenerics",
                inputGeneric, inputOverrideGeneric);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());

        // retrieve each object
        AvmTransactionResult result2 = createAndRunTransaction("getIntGen");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result2.getResultCode());
        Assert.assertEquals(inputGeneric, TestingHelper.decodeResult(result2));

        AvmTransactionResult result3 = createAndRunTransaction("getIntGenSub");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result3.getResultCode());
        Assert.assertEquals(inputOverrideGeneric, TestingHelper.decodeResult(result3));

        AvmTransactionResult result4 = createAndRunTransaction("getSubCopy");
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result4.getResultCode());
        Assert.assertEquals(inputOverrideGeneric, TestingHelper.decodeResult(result4));
    }

    private AvmTransactionResult createAndRunTransaction(String methodName, Object ... args){
        byte[] txData = ABIEncoder.encodeMethodArguments(methodName, args);
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(new TransactionContext[]{context})[0].get();
    }
}
