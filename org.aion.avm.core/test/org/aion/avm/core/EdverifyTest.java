package org.aion.avm.core;

import net.i2p.crypto.eddsa.Utils;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

public class EdverifyTest {
    private static byte[] publicKeyBytes = Utils.hexToBytes("8c11e9a4772bb651660a5a5e412be38d33f26b2de0487115d472a6c8bf60aa19");
    private byte[] testMessage = "test message".getBytes();
    private byte[] messageSignature = Utils.hexToBytes("0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61fb73777830ba3e92e4aa1832d41ec00c6e1d3bdd193e779cc2d51cb10d908c0a");

    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddress;

    private KernelInterfaceImpl kernel;
    private VirtualMachine avm;

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClasses(EdverifyTestTargetClass.class);

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddress = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testVerifyCorrectness(){
        byte[] txData = ABIEncoder.encodeMethodArguments("callEdverify", testMessage, messageSignature, publicKeyBytes);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertTrue((Boolean) TestingHelper.decodeResult(txResult));
    }

    @Test
    public void testVerifyFailIncorrectSignature(){
        byte[] incorrectSignature = messageSignature;
        incorrectSignature[0] = (byte) (int)(incorrectSignature[0] + 1);

        byte[] txData = ABIEncoder.encodeMethodArguments("callEdverify", testMessage, incorrectSignature, publicKeyBytes);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertFalse((Boolean) TestingHelper.decodeResult(txResult));
    }

    @Test
    public void testVerifyFailIncorrectPublicKey(){
        byte[] incorrectPublicKey = publicKeyBytes;
        incorrectPublicKey[1] = (byte) (int)(incorrectPublicKey[1] + 1);

        byte[] txData = ABIEncoder.encodeMethodArguments("callEdverify", testMessage, messageSignature, incorrectPublicKey);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult txResult = avm.run(new TransactionContext[]{context})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertFalse((Boolean) TestingHelper.decodeResult(txResult));
    }
}
