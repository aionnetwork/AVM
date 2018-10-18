package org.aion.avm.core.collection;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.*;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class AionCollectionPerfTest {

    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 100_000_000_000L;
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
                AionMap.class,
                AionPlainSet.class,
                AionPlainMap.class
        );
    }

    private byte[] buildMapPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionMapPerfContract.class,
                AionList.class,
                AionSet.class,
                AionMap.class,
                AionPlainSet.class,
                AionPlainMap.class
        );
    }

    private TransactionResult deploy(KernelInterface kernel, Avm avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from), 0L, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext).get();

        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());

        return createResult;
    }


    private TransactionResult call(KernelInterface kernel, Avm avm, byte[] contract, byte[] sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), 0, args, energyLimit, 1l);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        TransactionResult callResult = avm.run(callContext).get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
        return callResult;
    }

    @Test
    public void testList() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionList");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildListPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callAppend");
        TransactionResult appendResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Append        : " + appendResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callInsertHead");
        TransactionResult insertHeadResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Insert Head   : " + insertHeadResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callInsertMiddle");
        TransactionResult insertMiddleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Insert Middle : " + insertMiddleResult.getEnergyUsed() / AionListPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        avm.shutdown();
    }

    @Test
    public void testSet() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionSet");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildSetPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAdd");
        TransactionResult addResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callContains");
        TransactionResult containsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        TransactionResult removeReult = call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.getEnergyUsed() / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainSet");

        args = ABIEncoder.encodeMethodArguments("callInitB");
        initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAddB");
        addResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callContainsB");
        containsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemoveB");
        removeReult = call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.getEnergyUsed() / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

    @Test
    public void testMap() {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionMap");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildMapPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callInit");
        TransactionResult initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPut");
        TransactionResult putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGet");
        TransactionResult getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        TransactionResult removeResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainMap");

        args = ABIEncoder.encodeMethodArguments("callInitB");
        initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPutB");
        putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGetB");
        getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemoveB");
        removeResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

}
