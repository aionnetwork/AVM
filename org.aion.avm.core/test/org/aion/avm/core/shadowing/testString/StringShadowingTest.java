package org.aion.avm.core.shadowing.testString;

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
import org.junit.Test;

public class StringShadowingTest {

    @Test
    public void testSingleString() {
        byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        // deploy it
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), 0L, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        byte[] dappAddr = avm.run(context).getReturnData();

        // call transactions and validate the results
        txData = ABIEncoder.encodeMethodArguments("singleStringReturnInt");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, (int[]) TestingHelper.decodeResult(result)));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBoolean");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{1, 0, 1, 0, 1, 0, 0}, (byte[]) TestingHelper.decodeResult(result)));
        //Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, (boolean[]) TestingHelper.decodeResult(result)));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnChar");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);
        Assert.assertEquals('a', TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBytes");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, (byte[]) TestingHelper.decodeResult(result)));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnLowerCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);
        Assert.assertEquals("abc", TestingHelper.decodeResult(result));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnUpperCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);
        Assert.assertEquals("ABC", TestingHelper.decodeResult(result));
    }
}
