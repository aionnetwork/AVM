package org.aion.avm.embed;

import java.math.BigInteger;
import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.StandardCapabilities;
import org.aion.avm.embed.poc.AionBufferPerfContract;
import org.aion.avm.tooling.ABIUtil;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class AionBufferPerfTest {
    private AionAddress from = TestingState.PREMINED_ADDRESS;
    private long energyLimit = 100_000_000L;
    private long energyPrice = 1;

    private byte[] buildBufferPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionBufferPerfContract.class);
    }

    private TransactionResult deploy(IExternalState kernel, AvmImpl avm, byte[] testJar){
        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = AvmTransactionUtil.create(from, kernel.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new Transaction[] {createTransaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();

        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        return createResult;
    }

    private TransactionResult call(IExternalState kernel, AvmImpl avm, AionAddress contract, AionAddress sender, byte[] args) {
        Transaction callTransaction = AvmTransactionUtil.call(sender, contract, kernel.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1L);
        TransactionResult callResult = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
        return callResult;
    }

    @Test
    public void testBuffer() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurements for AionBuffer\n>>");
        byte[] args;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        IExternalState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());

        TransactionResult deployRes = deploy(kernel, avm, buildBufferPerfJar());
        AionAddress contract = new AionAddress(deployRes.copyOfTransactionOutput().orElseThrow());

        args = ABIUtil.encodeMethodArguments("callPutByte");
        TransactionResult putByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putByte()           : " + putByteResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetByte");
        TransactionResult getByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getByte()           : " + getByteResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutChar");
        TransactionResult putCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putChar()           : " + putCharResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetChar");
        TransactionResult getCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getChar()           : " + getCharResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutShort");
        TransactionResult putShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putShort()          : " + putShortResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetShort");
        TransactionResult getShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getShort()          : " + getShortResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutInt");
        TransactionResult putIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putInt()            : " + putIntResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetInt");
        TransactionResult getIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getInt()            : " + getIntResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutFloat");
        TransactionResult putFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putFloat()          : " + putFloatResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetFloat");
        TransactionResult getFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getFloat()          : " + getFloatResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutLong");
        TransactionResult putLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putLong()           : " + putLongResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetLong");
        TransactionResult getLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getLong()           : " + getLongResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutDouble");
        TransactionResult putDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putDouble()         : " + putDoubleResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetDouble");
        TransactionResult getDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getDouble()         : " + getDoubleResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutAddress");
        TransactionResult putAddressResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putAddress()         : " + putAddressResult.energyUsed / AionBufferPerfContract.BIG_ELT_COUNT);

        args = ABIUtil.encodeMethodArguments("callGetAddress");
        TransactionResult getAddressResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getAddress()         : " + getAddressResult.energyUsed / AionBufferPerfContract.BIG_ELT_COUNT);

        args = ABIUtil.encodeMethodArguments("callPutBigInt");
        TransactionResult putBigIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putBigInt()         : " + putBigIntResult.energyUsed / AionBufferPerfContract.BIG_ELT_COUNT);

        args = ABIUtil.encodeMethodArguments("callGetBigInt");
        TransactionResult getBigIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getBigInt()         : " + getBigIntResult.energyUsed / AionBufferPerfContract.BIG_ELT_COUNT);

        args = ABIUtil.encodeMethodArguments("callTransferBytesToBuffer");
        TransactionResult putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> put()               : "
            + putResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIUtil.encodeMethodArguments("callTransferBytesFromBuffer");
        TransactionResult getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> get()               : "
            + getResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIUtil.encodeMethodArguments("callEquals");
        TransactionResult equalsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> equals()            : " + equalsResult.energyUsed / AionBufferPerfContract.NUM_ELEMENTS);
        avm.shutdown();

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

}
