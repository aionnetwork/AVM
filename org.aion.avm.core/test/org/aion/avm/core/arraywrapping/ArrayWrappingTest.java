package org.aion.avm.core.arraywrapping;

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


public class ArrayWrappingTest {

    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        byte[] arrayWrappingTestJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);

        byte[] txData = new CodeAndArguments(arrayWrappingTestJar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from), 0L, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testBooleanArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testBooleanArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testByteArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testByteArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testCharArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testCharArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDoubleArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testDoubleArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testFloatArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testFloatArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testIntArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIntArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testLongArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testLongArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testShortArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShortArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testObjectArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testObjectArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testStringArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testStringArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testSignature() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testSignature");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testVarargs() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testVarargs");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testTypeChecking() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testTypeChecking");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testClassField() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testClassField");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiInt() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiInt");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiByte() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiByte");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiChar() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiChar");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiFloat() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiFloat");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiLong() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiLong");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiDouble() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiDouble");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testMultiRef() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiRef");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testHierarachy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testHierarachy");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testIncompleteArrayIni() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIncompleteArrayIni");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

//    @Test
//    public void testArrayEnergy() {
//
//        Object obj = clazz.getConstructor().newInstance();
//        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testArrayEnergy"));
//
//        helper.externalSetEnergy(10000000);
//        try{
//            method.invoke(obj);
//        }catch(InvocationTargetException e){
//            Assert.assertFalse(e.getCause() instanceof OutOfEnergyException);
//        }
//
//        helper.externalSetEnergy(1000);
//        try{
//            method.invoke(obj);
//        }catch(InvocationTargetException e){
//            Assert.assertTrue(e.getCause() instanceof OutOfEnergyException);
//        }
//        helper.externalSetEnergy(10000000000L);
//    }

    @Test
    public void testInterfaceArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testInterfaceArray");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testArrayClone() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayClone");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }
}
