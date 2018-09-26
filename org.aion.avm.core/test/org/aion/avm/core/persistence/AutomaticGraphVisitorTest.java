package org.aion.avm.core.persistence;

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


public class AutomaticGraphVisitorTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    @Before
    public void setup() {
        byte[] automaticGraphVisitorTestJar = JarBuilder.buildJarForMainAndClasses( AutomaticGraphVisitorTargetPrimary.class, AutomaticGraphVisitorTargetSecondary.class);

        byte[] txData = new CodeAndArguments(automaticGraphVisitorTestJar, null).encodeToBytes();

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(context).getReturnData();
    }

    @Test
    public void createPrimary() {
        byte[] txData = ABIEncoder.encodeMethodArguments("getValue");
        Transaction tx = new Transaction(Transaction.Type.CALL, from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(42, TestingHelper.decodeResult(result));
    }

    @Test
    public void manipulatePrimaryFinalField() {
        byte[] txData = ABIEncoder.encodeMethodArguments("setValue", 100);
        Transaction tx = new Transaction(Transaction.Type.CALL, from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        Assert.assertEquals(100, TestingHelper.decodeResult(result));

        /*Object primary = this.primaryClass.getConstructor().newInstance();
        Field valueField = this.primaryClass.getDeclaredField("avm_value");
        Assert.assertEquals(42,valueField.getInt(primary));
        valueField.setInt(primary, 100);
        Assert.assertEquals(100,valueField.getInt(primary));*/
    }

/*    @Test
    public void createSecondaryDirect() {
        Object secondary = this.secondaryClass.getConstructor(int.class).newInstance(5);
        Assert.assertEquals(5, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondaryThroughPrimary() {
        Object secondary = this.primaryClass.getMethod("avm_createSecondary", int.class, int.class).invoke(null, 5, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.primaryClass.getMethod("avm_changeAgain", this.secondaryClass, int.class).invoke(null, secondary, 7);
        Assert.assertEquals(7, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondarySpecialConstructor() {
        Object secondary = this.secondaryClass.getConstructor(IDeserializer.class, IPersistenceToken.class).newInstance(null, null);
        Assert.assertEquals(0, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 1);
        Assert.assertEquals(1, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }*/
}
