package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.poc.AionBufferPerfContract;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.Assert;
import org.junit.Test;


public class AionBufferPerfTest {
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 100_000_000_000L;
    private long energyPrice = 1;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(),
        System.currentTimeMillis(), new byte[0]);

    private byte[] buildBufferPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionBufferPerfContract.class,
            AionBuffer.class);
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
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender).longValue(), BigInteger.ZERO, args, energyLimit, 1L);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        AvmTransactionResult callResult = avm.run(new TransactionContext[] {callContext})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testBuffer() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurements for AionBuffer\n>>");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        AvmTransactionResult deployRes = deploy(kernel, avm, buildBufferPerfJar());
        org.aion.vm.api.interfaces.Address contract = AvmAddress.wrap(deployRes.getReturnData());

        args = ABIEncoder.encodeMethodArguments("callPutByte");
        AvmTransactionResult putByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putByte()           : " + putByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetByte");
        AvmTransactionResult getByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getByte()           : " + getByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutChar");
        AvmTransactionResult putCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putChar()           : " + putCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetChar");
        AvmTransactionResult getCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getChar()           : " + getCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutShort");
        AvmTransactionResult putShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putShort()          : " + putShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetShort");
        AvmTransactionResult getShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getShort()          : " + getShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutInt");
        AvmTransactionResult putIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putInt()            : " + putIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetInt");
        AvmTransactionResult getIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getInt()            : " + getIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutFloat");
        AvmTransactionResult putFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putFloat()          : " + putFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetFloat");
        AvmTransactionResult getFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getFloat()          : " + getFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutLong");
        AvmTransactionResult putLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putLong()           : " + putLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetLong");
        AvmTransactionResult getLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getLong()           : " + getLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutDouble");
        AvmTransactionResult putDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putDouble()         : " + putDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetDouble");
        AvmTransactionResult getDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getDouble()         : " + getDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callTransferBytesToBuffer");
        AvmTransactionResult putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> put()               : "
            + putResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIEncoder.encodeMethodArguments("callTransferBytesFromBuffer");
        AvmTransactionResult getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> get()               : "
            + getResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIEncoder.encodeMethodArguments("callEquals");
        AvmTransactionResult equalsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> equals()            : " + equalsResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);
        avm.shutdown();

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

}
