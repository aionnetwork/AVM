package org.aion.avm.core.arraywrapping;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.*;


public class ArrayWrappingTest {

    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        
        byte[] arrayWrappingTestJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);

        byte[] txData = new CodeAndArguments(arrayWrappingTestJar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBooleanArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testBooleanArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testByteArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testByteArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testCharArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testCharArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDoubleArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testDoubleArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testFloatArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testFloatArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testIntArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIntArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testLongArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testLongArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testShortArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShortArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testObjectArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testObjectArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testStringArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testStringArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testSignature() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testSignature");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testVarargs() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testVarargs");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testTypeChecking() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testTypeChecking");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testClassField() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testClassField");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiInt() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiInt");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiByte() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiByte");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiChar() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiChar");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiFloat() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiFloat");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiLong() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiLong");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiDouble() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiDouble");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiRef() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiRef");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testHierarachy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testHierarachy");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testIncompleteArrayIni() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIncompleteArrayIni");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testArrayEnergy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayEnergy");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testInterfaceArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testInterfaceArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testArrayClone() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayClone");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }
}
