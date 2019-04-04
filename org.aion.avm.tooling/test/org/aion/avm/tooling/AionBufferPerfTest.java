package org.aion.avm.tooling;

import java.math.BigInteger;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.poc.AionBufferPerfContract;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class AionBufferPerfTest {
    private org.aion.types.Address from = TestingKernel.PREMINED_ADDRESS;
    private long energyLimit = 100_000_000L;
    private long energyPrice = 1;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(),
        System.currentTimeMillis(), new byte[0]);

    private byte[] buildBufferPerfJar() {
        return JarBuilder.buildJarForMainAndClassesAndUserlib(AionBufferPerfContract.class);
    }

    private TransactionResult deploy(KernelInterface kernel, AvmImpl avm, byte[] testJar){
        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new Transaction[] {createTransaction})[0].get();

        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return createResult;
    }

    private TransactionResult call(KernelInterface kernel, AvmImpl avm, org.aion.types.Address contract, org.aion.types.Address sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1L);
        TransactionResult callResult = avm.run(kernel, new Transaction[] {callTransaction})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testBuffer() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurements for AionBuffer\n>>");
        byte[] args;
        KernelInterface kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());

        TransactionResult deployRes = deploy(kernel, avm, buildBufferPerfJar());
        org.aion.types.Address contract = org.aion.types.Address.wrap(deployRes.getReturnData());

        args = ABIUtil.encodeMethodArguments("callPutByte");
        AvmTransactionResult putByteResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putByte()           : " + putByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetByte");
        AvmTransactionResult getByteResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getByte()           : " + getByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutChar");
        AvmTransactionResult putCharResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putChar()           : " + putCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetChar");
        AvmTransactionResult getCharResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getChar()           : " + getCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutShort");
        AvmTransactionResult putShortResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putShort()          : " + putShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetShort");
        AvmTransactionResult getShortResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getShort()          : " + getShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutInt");
        AvmTransactionResult putIntResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putInt()            : " + putIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetInt");
        AvmTransactionResult getIntResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getInt()            : " + getIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutFloat");
        AvmTransactionResult putFloatResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putFloat()          : " + putFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetFloat");
        AvmTransactionResult getFloatResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getFloat()          : " + getFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutLong");
        AvmTransactionResult putLongResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putLong()           : " + putLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetLong");
        AvmTransactionResult getLongResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getLong()           : " + getLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callPutDouble");
        AvmTransactionResult putDoubleResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> putDouble()         : " + putDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callGetDouble");
        AvmTransactionResult getDoubleResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> getDouble()         : " + getDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIUtil.encodeMethodArguments("callTransferBytesToBuffer");
        AvmTransactionResult putResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> put()               : "
            + putResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIUtil.encodeMethodArguments("callTransferBytesFromBuffer");
        AvmTransactionResult getResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> get()               : "
            + getResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIUtil.encodeMethodArguments("callEquals");
        AvmTransactionResult equalsResult = (AvmTransactionResult) call(kernel, avm, contract, from, args);
        System.out.println(">> equals()            : " + equalsResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);
        avm.shutdown();

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

}
