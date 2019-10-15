package org.aion.avm.core.collection;

import java.math.BigInteger;
import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class AionCollectionPerfTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    private AionAddress from = TestingState.PREMINED_ADDRESS;
    private TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 100_000_000L;
    private long energyPrice = 1;

    private byte[] buildListPerfJar() {
        return UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(AionListPerfContract.class);
    }

    private byte[] buildSetPerfJar() {
        return UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(AionSetPerfContract.class);
    }

    private byte[] buildMapPerfJar() {
        return UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(AionMapPerfContract.class);
    }

    private TransactionResult deploy(IExternalState externalState, AvmImpl avm, byte[] testJar){


        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = AvmTransactionUtil.create(from, externalState.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult createResult = avm.run(externalState, new Transaction[] {createTransaction}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber()-1)[0].getResult();

        Assert.assertTrue(createResult.transactionStatus.isSuccess());

        return createResult;
    }


    private TransactionResult call(IExternalState externalState, AvmImpl avm, AionAddress contract, AionAddress sender, byte[] args) {
        Transaction callTransaction = AvmTransactionUtil.call(sender, contract, externalState.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1l);
        TransactionResult callResult = avm.run(externalState, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
        return callResult;
    }

    @Test
    public void testList() {
        report(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        report(">> Energy measurement for AionList");
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
        report(">> Append        : " + appendResult.energyUsed / AionListPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callInit");
        call(externalState, avm, contract, from, args);
        args = encodeNoArgsMethodCall("callInsertHead");
        TransactionResult insertHeadResult = call(externalState, avm, contract, from, args);
        report(">> Insert Head   : " + insertHeadResult.energyUsed / AionListPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callInit");
        call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callInsertMiddle");
        TransactionResult insertMiddleResult = call(externalState, avm, contract, from, args);
        report(">> Insert Middle : " + insertMiddleResult.energyUsed / AionListPerfContract.SIZE);

        report("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        avm.shutdown();
    }

    @Test
    public void testSet() {
        report(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        report(">> Energy measurement for AionSet");
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
        report(">> Add           : " + addResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callContains");
        TransactionResult containsResult = call(externalState, avm, contract, from, args);
        report(">> Contains      : " + containsResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemove");
        TransactionResult removeReult = call(externalState, avm, contract, from, args);
        report(">> Remove        : " + removeReult.energyUsed / AionSetPerfContract.SIZE);

        report("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        report(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        report(">> Energy measurement for AionPlainSet");

        args = encodeNoArgsMethodCall("callInitB");
        initResult = call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callAddB");
        addResult = call(externalState, avm, contract, from, args);
        report(">> Add           : " + addResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callContainsB");
        containsResult = call(externalState, avm, contract, from, args);
        report(">> Contains      : " + containsResult.energyUsed / AionSetPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemoveB");
        removeReult = call(externalState, avm, contract, from, args);
        report(">> Remove        : " + removeReult.energyUsed / AionSetPerfContract.SIZE);

        report("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }

    @Test
    public void testMap() {

        report(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        report(">> Energy measurement for AionMap");
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
        report(">> Put           : " + putResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callGet");
        TransactionResult getResult = call(externalState, avm, contract, from, args);
        report(">> Get           : " + getResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemove");
        TransactionResult removeResult = call(externalState, avm, contract, from, args);
        report(">> Remove        : " + removeResult.energyUsed / AionMapPerfContract.SIZE);

        report("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        report(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        report(">> Energy measurement for AionPlainMap");

        args = encodeNoArgsMethodCall("callInitB");
        initResult = call(externalState, avm, contract, from, args);

        args = encodeNoArgsMethodCall("callPutB");
        putResult = call(externalState, avm, contract, from, args);
        report(">> Put           : " + putResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callGetB");
        getResult = call(externalState, avm, contract, from, args);
        report(">> Get           : " + getResult.energyUsed / AionMapPerfContract.SIZE);

        args = encodeNoArgsMethodCall("callRemoveB");
        removeResult = call(externalState, avm, contract, from, args);
        report(">> Remove        : " + removeResult.energyUsed / AionMapPerfContract.SIZE);

        report("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        avm.shutdown();
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }

    private static void report(String output) {
        if (REPORT) {
            System.out.println(output);
        }
    }
}
