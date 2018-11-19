package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * As part of issue-77, we want to see what a more typical application can see, from inside our environment.
 * This test operates on BasicAppTestTarget to observe what we are doing, from the inside.
 * Eventually, this will change into a shape where we will use the standard AvmImpl to completely run this
 * life-cycle, but we want to prove that it works, in isolation, before changing its details to account for
 * this design (especially considering that the entry-point interface is likely temporary).
 */
public class BasicAppTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClasses(BasicAppTestTarget.class
                , AionMap.class
                , AionSet.class
                , AionList.class
        );

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testIdentity() {
        byte[] txData = ABIEncoder.encodeMethodArguments("identity", new byte[] {42, 13});
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        // These should be the same instance.
        Assert.assertEquals(42, ((byte[]) TestingHelper.decodeResult(result))[0]);
        Assert.assertEquals(13, ((byte[]) TestingHelper.decodeResult(result))[1]);
    }

    @Test
    public void testSumInput() {
        byte[] txData = ABIEncoder.encodeMethodArguments("sum", new byte[] {42, 13});
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        // Should be just 1 byte, containing the sum.
        Assert.assertEquals(42 + 13, TestingHelper.decodeResult(result));
    }

    /**
     * This test makes multiple calls to the same contract instance, proving that static state survives between the calls.
     * It is mostly just a test to make sure that this property continues to be true, in the future, once we decide how
     * to save and resume state.
     */
    @Test
    public void testRepeatedSwaps() {
        byte[] txData = ABIEncoder.encodeMethodArguments("swapInputs", 1);
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(0, TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("swapInputs", 2);
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(1, TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("swapInputs", 1);
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    @Test
    public void testArrayEquality() {
        byte[] txData = ABIEncoder.encodeMethodArguments("arrayEquality", new byte[] {42, 13});
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(false, TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("arrayEquality", new byte[] {5, 6, 7, 8});
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(false, TestingHelper.decodeResult(result));
    }

    @Test
    public void testAllocateArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("allocateObjectArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    @Test
    public void testByteAutoboxing() {
        byte[] txData = ABIEncoder.encodeMethodArguments("byteAutoboxing", (byte) 42);
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(42, ((byte[]) TestingHelper.decodeResult(result))[0]);
        Assert.assertEquals(42, ((byte[]) TestingHelper.decodeResult(result))[1]);
    }

    @Test
    public void testMapInteraction() {
        byte[] txData = ABIEncoder.encodeMethodArguments("mapPut", (byte)1, (byte)42);
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals((byte) 42, TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("mapPut", (byte)2, (byte)13);
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals((byte) 13, TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("mapGet", (byte)2);
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals((byte) 13, TestingHelper.decodeResult(result));
    }
}
