package org.aion.avm.core.shadowing.testNio;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class NioShadowingTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 600_000_00000L;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    @Before
    public void setup() {
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from), 0L, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(context).getReturnData();
    }

    @Test
    public void testByteBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testByteBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testCharBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testCharBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDoubleBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testDoubleBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testFloatBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testFloatBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }


    @Test
    public void testIntBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIntBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }


    @Test
    public void testLongBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testLongBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testShortBuffer() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShortBuffer");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }
}
