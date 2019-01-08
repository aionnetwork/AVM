package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests calling a contract that performs a Call transaction on an address that is 'unsafe' for the
 * Avm to run (ie. that contains code that the Avm cannot interpret and which must be run by another
 * virtual machine).
 */
public class BadDestinationTest {
    private static Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private static KernelInterface kernel;
    private static AvmImpl avm;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstance(kernel);
        deployContract();
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    /**
     * Destination address has code and does not begin with the Avm prefix. It is therefore a bad
     * destination.
     */
    @Test
    public void testCallingIntoBadDestinationWithoutCatchingException() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte valueAsByte = (byte) i;

            if (valueAsByte != NodeEnvironment.CONTRACT_PREFIX) {
                Address destination = generateDestinationAddressWithSpecifiedFirstByte(valueAsByte);
                addCodeToAddress(destination);

                TransactionResult result = callContractWithoutCatchingException(destination);
                assertEquals(Code.FAILED_EXCEPTION, result.getResultCode());
            }
        }
    }

    /**
     * Destination address has code and does not begin with the Avm prefix. It is therefore a bad
     * destination.
     */
    @Test
    public void testCallingIntoBadDestinationAndCatchingException() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte valueAsByte = (byte) i;

            if (valueAsByte != NodeEnvironment.CONTRACT_PREFIX) {
                Address destination = generateDestinationAddressWithSpecifiedFirstByte(valueAsByte);
                addCodeToAddress(destination);

                TransactionResult result = callDestinationAndCatchException(destination);
                assertTrue(result.getResultCode().isSuccess());
                assertEquals(energyLimit, ((AvmTransactionResult) result).getEnergyUsed() + result.getEnergyRemaining());
            }
        }
    }

    /**
     * Destination address does not have any code. It is therefore an OK destination.
     */
    @Test
    public void testCallingIntoNonAvmPrefixedAddressWithNoCode() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte valueAsByte = (byte) i;

            Address destination = generateDestinationAddressWithSpecifiedFirstByte(valueAsByte);

            // Since the method doesn't catch the exception, it will fail if the destination is bad.
            TransactionResult result = callContractWithoutCatchingException(destination);
            assertTrue(result.getResultCode().isSuccess());
            assertEquals(energyLimit, ((AvmTransactionResult) result).getEnergyUsed() + result.getEnergyRemaining());
        }
    }

    private static void deployContract() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BadDestinationTarget.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, jar, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertTrue(result.getResultCode().isSuccess());
        contract = AvmAddress.wrap(result.getReturnData());
    }

    private TransactionResult callContractWithoutCatchingException(Address callAddress) {
        byte[] callData = encodeCallData("callDestinationNoExceptionCatching", callAddress);
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private TransactionResult callDestinationAndCatchException(Address callAddress) {
        byte[] callData = encodeCallData("callDestinationAndCatchException", callAddress);
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private byte[] encodeCallData(String methodName, Address address) {
        IInstrumentation instrumentation = newFakeInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        byte[] encoding = ABIEncoder.encodeMethodArguments(methodName, new org.aion.avm.api.Address(address.toBytes()));
        InstrumentationHelpers.detachThread(instrumentation);
        return encoding;
    }

    private Address generateDestinationAddressWithSpecifiedFirstByte(byte firstByte) {
        byte[] bytes = Helpers.randomBytes(Address.SIZE);
        bytes[0] = firstByte;
        return AvmAddress.wrap(bytes);
    }

    private void addCodeToAddress(Address address) {
        kernel.putCode(address, new byte[1]);
    }

    private IInstrumentation newFakeInstrumentation() {
        return new IInstrumentation () {
            @Override
            public void bootstrapOnly() {
            }
            @Override
            public void chargeEnergy(long arg0) throws OutOfEnergyException {
            }
            @Override
            public long energyLeft() {
                return 0;
            }
            @Override
            public void enterCatchBlock(int arg0, int arg1) {
            }
            @Override
            public void enterMethod(int arg0) {
            }
            @Override
            public void enterNewFrame(ClassLoader arg0, long arg1, int arg2) {
            }
            @Override
            public void exitCurrentFrame() {
            }
            @Override
            public void exitMethod(int arg0) {
            }
            @Override
            public void forceNextHashCode(int arg0) {
            }
            @Override
            public int getCurStackDepth() {
                return 0;
            }
            @Override
            public int getCurStackSize() {
                return 0;
            }
            @Override
            public int getNextHashCodeAndIncrement() {
                return 0;
            }
            @Override
            public int peekNextHashCode() {
                return 0;
            }
            @Override
            public void setAbortState() {
            }
            @Override
            public void clearAbortState() {
            }
            @Override
            public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable arg0) {
                return null;
            }
            @Override
            public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> arg0) {
                return null;
            }
            @Override
            public org.aion.avm.shadow.java.lang.String wrapAsString(String arg0) {
                return null;
            }
            @Override
            public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg0) {
                return null;
            }
        };
    }

}
