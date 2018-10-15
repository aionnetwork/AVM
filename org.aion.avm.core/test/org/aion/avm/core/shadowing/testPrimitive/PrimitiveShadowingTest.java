package org.aion.avm.core.shadowing.testPrimitive;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.*;

public class PrimitiveShadowingTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 600_000_00000L;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from), 0L, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(context).getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBoolean() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testBoolean");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testByte() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testByte");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDouble() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testDouble");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testFloat() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testFloat");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testInteger() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testInteger");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testLong() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testLong");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testShort() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShort");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testAutoboxing() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testAutoboxing");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }
}
