package org.aion.avm.core.collection;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Test;

public class AionCollectionInterfaceTest {

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = Long.MAX_VALUE - 100l;
    private long energyPrice = 1;

    private byte[] buildJar() {
        return JarBuilder.buildJarForMainAndClasses(AionCollectionInterfaceContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class
        );
    }

    private TransactionResult deploy(Avm avm, byte[] testJar){

        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0,
                new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);

        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());

        return createResult;
    }

    private TransactionResult call(Avm avm, byte[] contract, byte[] sender, byte[] args) {
        Transaction callTransaction = new Transaction(Transaction.Type.CALL, sender, contract, 0, args, energyLimit, 1l);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        TransactionResult callResult = avm.run(callContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
        return callResult;
    }

    @Test
    public void testList() {
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("testList");
        TransactionResult testResult = call(avm, contract, from, args);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, testResult.getStatusCode());
    }

    @Test
    public void testSet() {
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("testSet");
        TransactionResult testResult = call(avm, contract, from, args);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, testResult.getStatusCode());
    }

    @Test
    public void testMap() {
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("testMap");
        TransactionResult testResult = call(avm, contract, from, args);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, testResult.getStatusCode());
    }

}
