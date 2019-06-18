package org.aion.avm.core.collection;

import java.math.BigInteger;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.IExternalState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class AionCollectionPerfTest {

    private AionAddress from = TestingState.PREMINED_ADDRESS;
    private TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 100_000_000L;
    private long energyPrice = 1;

    private byte[] buildListPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionListPerfContract.class);
    }

    private byte[] buildSetPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionSetPerfContract.class);
    }

    private byte[] buildMapPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionMapPerfContract.class);
    }

    private TransactionResult deploy(IExternalState externalState, AvmImpl avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = AvmTransactionUtil.create(from, externalState.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult createResult = avm.run(externalState, new Transaction[] {createTransaction})[0].getResult();

        Assert.assertTrue(createResult.transactionStatus.isSuccess());

        return createResult;
    }


    private TransactionResult call(IExternalState externalState, AvmImpl avm, AionAddress contract, AionAddress sender, byte[] args) {
        Transaction callTransaction = AvmTransactionUtil.call(sender, contract, externalState.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1l);
        TransactionResult callResult = avm.run(externalState, new Transaction[] {callTransaction})[0].getResult();
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
        return callResult;
    }

    @Test
    public void testList() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionList");
        byte[] args;
        IExternalState externalState = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        TransactionResult deployRes = deploy(externalState, avm, buildListPerfJar());
        AionAddress contract = new AionAddress(deployRes.copyOfTransactionOutput().orElseThrow());

        args = encodeNoArgsMethodCall("callInit");
        TransactionResult initResult = call(externalState, avm, contract, from, args);
        Assert.assertTrue(initResult.transactionStatus.isSuccess());
        args = encodeNoArgsMethodCall("callAppend");
        TransactionResult appendResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Append        : " + appendResult.energyUsed / AionListPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callInit");
        call(externalState, avm, contract, from, args);
        args = encodeNoArgsMethodCall("callInsertHead");
        TransactionResult insertHeadResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Insert Head   : " + insertHeadResult.energyUsed / AionListPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callInit");
        call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callInsertMiddle");
        TransactionResult insertMiddleResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Insert Middle : " + insertMiddleResult.energyUsed / AionListPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        avm.shutdown();
    }

    @Test
    public void testSet() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionSet");
        byte[] args;
        IExternalState externalState = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        TransactionResult deployRes = deploy(externalState, avm, buildSetPerfJar());
        AionAddress contract = new AionAddress(deployRes.copyOfTransactionOutput().orElseThrow());

        args = encodeNoArgsMethodCall("callInit");
        TransactionResult initResult = call(externalState, avm, contract, from, args);
        Assert.assertTrue(initResult.transactionStatus.isSuccess());

        args = encodeNoArgsMethodCall("callAdd");
        TransactionResult addResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callContains");
        TransactionResult containsResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemove");
        TransactionResult removeReult = call(externalState, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.energyUsed / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainSet");

        args = encodeNoArgsMethodCall("callInitB");
        initResult = call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callAddB");
        addResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Add           : " + addResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callContainsB");
        containsResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Contains      : " + containsResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemoveB");
        removeReult = call(externalState, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeReult.energyUsed / AionSetPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

    @Test
    public void testMap() {

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionMap");
        byte[] args;
        IExternalState externalState = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        TransactionResult deployRes = deploy(externalState, avm, buildMapPerfJar());
        AionAddress contract = new AionAddress(deployRes.copyOfTransactionOutput().orElseThrow());

        args = encodeNoArgsMethodCall("callInit");
        TransactionResult initResult = call(externalState, avm, contract, from, args);
        Assert.assertTrue(initResult.transactionStatus.isSuccess());

        args = encodeNoArgsMethodCall("callPut");
        TransactionResult putResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callGet");
        TransactionResult getResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemove");
        TransactionResult removeResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.energyUsed / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurement for AionPlainMap");

        args = encodeNoArgsMethodCall("callInitB");
        initResult = call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callPutB");
        putResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Put           : " + putResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callGetB");
        getResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Get           : " + getResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemoveB");
        removeResult = call(externalState, avm, contract, from, args);
        System.out.println(">> Remove        : " + removeResult.energyUsed / AionMapPerfContract.SIZE);

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}
