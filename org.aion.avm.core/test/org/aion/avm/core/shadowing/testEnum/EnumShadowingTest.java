package org.aion.avm.core.shadowing.testEnum;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.*;

public class EnumShadowingTest {
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
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class, TestEnum.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testEnumAccess() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testEnumAccess");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testEnumValues() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testEnumValues");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }

    @Test
    public void testShadowJDKEnum() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShadowJDKEnum");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        Assert.assertEquals(true, TestingHelper.decodeResult(result));
    }
}
