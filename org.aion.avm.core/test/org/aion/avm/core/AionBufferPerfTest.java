package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.poc.AionBufferPerfContract;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class AionBufferPerfTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 100_000_000_000L;
    private long energyPrice = 1;
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH),
        System.currentTimeMillis(), new byte[0]);

    private byte[] buildBufferPerfJar() {
        return JarBuilder.buildJarForMainAndClasses(AionBufferPerfContract.class,
            AionBuffer.class);
    }

    private TransactionResult deploy(KernelInterface kernel, Avm avm, byte[] testJar){
        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, new CodeAndArguments(testJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(new TransactionContext[] {createContext})[0].get();

        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        return createResult;
    }

    private TransactionResult call(KernelInterface kernel, Avm avm, byte[] contract, byte[] sender, byte[] args) {
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), BigInteger.ZERO, args, energyLimit, 1L);
        TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
        TransactionResult callResult = avm.run(new TransactionContext[] {callContext})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
        return callResult;
    }

    @Test
    public void testBuffer() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">> Energy measurements for AionBuffer\n>>");
        byte[] args;
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        TransactionResult deployRes = deploy(kernel, avm, buildBufferPerfJar());
        byte[] contract = deployRes.getReturnData();

        args = ABIEncoder.encodeMethodArguments("callPutByte");
        TransactionResult putByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putByte()           : " + putByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetByte");
        TransactionResult getByteResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getByte()           : " + getByteResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutChar");
        TransactionResult putCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putChar()           : " + putCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetChar");
        TransactionResult getCharResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getChar()           : " + getCharResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutShort");
        TransactionResult putShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putShort()          : " + putShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetShort");
        TransactionResult getShortResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getShort()          : " + getShortResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutInt");
        TransactionResult putIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putInt()            : " + putIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetInt");
        TransactionResult getIntResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getInt()            : " + getIntResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutFloat");
        TransactionResult putFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putFloat()          : " + putFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetFloat");
        TransactionResult getFloatResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getFloat()          : " + getFloatResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutLong");
        TransactionResult putLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putLong()           : " + putLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetLong");
        TransactionResult getLongResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getLong()           : " + getLongResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callPutDouble");
        TransactionResult putDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> putDouble()         : " + putDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callGetDouble");
        TransactionResult getDoubleResult = call(kernel, avm, contract, from, args);
        System.out.println(">> getDouble()         : " + getDoubleResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);

        args = ABIEncoder.encodeMethodArguments("callTransferBytesToBuffer");
        TransactionResult putResult = call(kernel, avm, contract, from, args);
        System.out.println(">> put()               : "
            + putResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIEncoder.encodeMethodArguments("callTransferBytesFromBuffer");
        TransactionResult getResult = call(kernel, avm, contract, from, args);
        System.out.println(">> get()               : "
            + getResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS
            + "     (for transfers of " + AionBufferPerfContract.TRANSFER_SIZE + " bytes)");

        args = ABIEncoder.encodeMethodArguments("callEquals");
        TransactionResult equalsResult = call(kernel, avm, contract, from, args);
        System.out.println(">> equals()            : " + equalsResult.getEnergyUsed() / AionBufferPerfContract.NUM_ELEMENTS);
        avm.shutdown();

        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

}
