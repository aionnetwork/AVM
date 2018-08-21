package org.aion.avm.core.collection;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.BMap;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Test;

public class AionCollectionPerfTest {

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = Long.MAX_VALUE - 100l;
    private long energyPrice = 1;

    private byte[] buildListPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionListPerfContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class
        );
    }

    private byte[] buildSetPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionSetPerfContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class
        );
    }

    private byte[] buildMapPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionMapPerfContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class,
                BMap.class
        );
    }

    private TransactionResult deploy(Avm avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0,
                Helpers.encodeCodeAndData(testJar, testWalletArguments), energyLimit, energyPrice);
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
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionList");
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildListPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callAppend");
        TransactionResult appendResult = call(avm, contract, from, args);
        System.out.println(">> Append        : " + appendResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callInsertHead");
        TransactionResult insertHeadResult = call(avm, contract, from, args);
        System.out.println(">> Insert Head   : " + insertHeadResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callInsertMiddle");
        TransactionResult insertMiddleResult = call(avm, contract, from, args);
        System.out.println(">> Insert Middle : " + insertMiddleResult.getEnergyUsed() / AionListPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

    }

    @Test
    public void testSet() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionSet");
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildSetPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAdd");
        TransactionResult addResult = call(avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        initResult = call(avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callContains");
        TransactionResult containsResult = call(avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        initResult = call(avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callRemove");
        TransactionResult removeReult = call(avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.getEnergyUsed() / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void testMap() {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionMap");
        byte[] args;
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

        TransactionResult deployRes = deploy(avm, buildMapPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPut");
        TransactionResult putResult = call(avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGet");
        TransactionResult getResult = call(avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for BMap");

        args = ABIEncoder.encodeMethodArguments("callInitB");
        initResult = call(avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPutB");
        putResult = call(avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGetB");
        getResult = call(avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

}
