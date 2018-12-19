package org.aion.avm.core.collection;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.*;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.Assert;
import org.junit.Test;

public class AionCollectionPerfTest {

    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
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

    private AvmTransactionResult deploy(KernelInterface kernel, Avm avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        AvmTransactionResult createResult = avm.run(new TransactionContext[] {createContext})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());

        return createResult;
    }


    private AvmTransactionResult call(KernelInterface kernel, Avm avm, org.aion.vm.api.interfaces.Address contract, org.aion.vm.api.interfaces.Address sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender).longValue(), BigInteger.ZERO, args, energyLimit, 1l);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        AvmTransactionResult callResult = avm.run(new TransactionContext[] {callContext})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testList() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionList");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        AvmTransactionResult deployRes = deploy(kernel, avm, buildListPerfJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callAppend");
        AvmTransactionResult appendResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Append        : " + appendResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);
        args = ABIEncoder.encodeMethodArguments("callInsertHead");
        AvmTransactionResult insertHeadResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Insert Head   : " + insertHeadResult.getEnergyUsed() / AionListPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callInit");
        call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callInsertMiddle");
        AvmTransactionResult insertMiddleResult = call(kernel, avm, contract, from, args);
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
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        AvmTransactionResult deployRes = deploy(kernel, avm, buildSetPerfJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callAdd");
        AvmTransactionResult addResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callContains");
        AvmTransactionResult containsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.getEnergyUsed() / AionSetPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        AvmTransactionResult removeReult = call(kernel, avm, contract, from, args);
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
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        AvmTransactionResult deployRes = deploy(kernel, avm, buildMapPerfJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callInit");
        AvmTransactionResult initResult = call(kernel, avm, contract, from, args);

        args = ABIEncoder.encodeMethodArguments("callPut");
        AvmTransactionResult putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callGet");
        AvmTransactionResult getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.getEnergyUsed() / AionMapPerfContract.SIZE);

        args = ABIEncoder.encodeMethodArguments("callRemove");
        AvmTransactionResult removeResult = call(kernel, avm, contract, from, args);
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
