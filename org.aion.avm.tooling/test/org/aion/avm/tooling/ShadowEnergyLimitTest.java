package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.kernel.AvmTransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class ShadowEnergyLimitTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    private static long warmup = 0;
    private static long benchmark = 1;
    private static boolean printValues = false;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(ShadowEnergyLimitTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void callEdverifyMaxLengthZero() {
        long time = run("callEdverifyMaxLengthZero");
        if (printValues)
            System.out.println("callEdverifyMaxLengthZero time(ns) " + time);
    }

    @Test
    public void testEdverifyMaxMessageLengthOne() {
        long time = run("callEdverifyMaxLengthOne");
        if (printValues)
            System.out.println("callEdverifyMaxLengthOne time(ns) " + time);
    }

    @Test
    public void callEdverifyLoop() {
        long time = run("callEdverifyLoop", 350);
        if (printValues)
            System.out.println("callEdverifyLoop time(ns) " + time);
    }

    @Test
    public void callBigIntegerSqrt() {
        long time = run("callBigIntegerSqrt", 600);
        if (printValues)
            System.out.println("callBigIntegerSqrt time(ns) " + time);
    }

    @Test
    public void callBigIntegerToString() {
        long time = run("callBigIntegerToString", 1200);
        if (printValues)
            System.out.println("callBigIntegerToString time(ns) " + time);
    }

    @Test
    public void callOutOfMemory() {
        callForEnergyFailure("forceOutOfMemory", 1000);
        callForEnergyFailure("forceOutOfMemory", 1001);
    }

    private long run(String methodName, Object... args) {
        for (int i = 0; i < warmup; i++) {
            callStatic(methodName, args);
        }
        long time = 0;
        for (int i = 0; i < benchmark; i++) {
            time += callStatic(methodName, args);
        }
        return time / benchmark;
    }

    private void callForEnergyFailure(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper callResult = avmRule.call(sender, contract, value, data, 2_000_000, 1);
        AvmTransactionResult result = (AvmTransactionResult)callResult.getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, result.getResultCode());
    }

    private long callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        long startTime = System.nanoTime();
        AvmRule.ResultWrapper result = avmRule.call(sender, contract, value, data, 2_000_000, 1);
        long endTime = System.nanoTime();
        if (printValues)
            System.out.println(methodName + " energy consumption " + (2000000 - result.getTransactionResult().getEnergyRemaining()));
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        return endTime - startTime;
    }
}
