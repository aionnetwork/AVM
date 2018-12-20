package org.aion.avm.core.collection;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.Assert;
import org.junit.Test;

public class AionCollectionInterfaceTest {

    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1;

    private byte[] buildJar() {
        return JarBuilder.buildJarForMainAndClasses(AionCollectionInterfaceContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class
        );
    }

    private TransactionResult deploy(KernelInterface kernel, VirtualMachine avm, byte[] testJar){

        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(new TransactionContext[] {createContext})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        return createResult;
    }

    private TransactionResult call(KernelInterface kernel, VirtualMachine avm, org.aion.vm.api.interfaces.Address contract, org.aion.vm.api.interfaces.Address sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(from).longValue(), BigInteger.ZERO, args, energyLimit, 1l);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        TransactionResult callResult = avm.run(new TransactionContext[] {callContext})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testList() {
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        VirtualMachine avm = CommonAvmFactory.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("testList");
        TransactionResult testResult = call(kernel, avm, contract, from, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
        avm.shutdown();
    }

    @Test
    public void testSet() {
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        VirtualMachine avm = CommonAvmFactory.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("testSet");
        TransactionResult testResult = call(kernel, avm, contract, from, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
        avm.shutdown();
    }

    @Test
    public void testMap() {
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        VirtualMachine avm = CommonAvmFactory.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("testMap");
        TransactionResult testResult = call(kernel, avm, contract, from, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
        avm.shutdown();
    }

}
