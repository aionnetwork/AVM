package org.aion.avm.embed;

import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import avm.Address;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.TransactionResult;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests calling a contract that performs a Call transaction on an address that is 'unsafe' for the
 * Avm to run (ie. that contains code that the Avm cannot interpret and which must be run by another
 * virtual machine).
 */
public class BadDestinationTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false).setBlockchainPrintlnEnabled(REPORT);
    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        deployContract();
    }

    /**
     * Destination address has code and does not begin with the Avm prefix. It is therefore a bad
     * destination.
     */
    @Test
    public void testCallingIntoBadDestinationWithoutCatchingException() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte valueAsByte = (byte) i;

            if (valueAsByte != TestingState.AVM_CONTRACT_PREFIX) {
                Address destination = generateDestinationAddressWithSpecifiedFirstByte(valueAsByte);
                addCodeToAddress(destination);

                TransactionResult result = callContractWithoutCatchingException(destination);
                assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
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

            if (valueAsByte != TestingState.AVM_CONTRACT_PREFIX) {
                Address destination = generateDestinationAddressWithSpecifiedFirstByte(valueAsByte);
                addCodeToAddress(destination);

                TransactionResult result = callDestinationAndCatchException(destination);
                assertTrue(result.transactionStatus.isSuccess());
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
            assertTrue(result.transactionStatus.isSuccess());
        }
    }

    private static void deployContract() {
        byte[] jar = avmRule.getDappBytes(BadDestinationTarget.class, new byte[0]);

        TransactionResult result = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
        contract = new Address(result.copyOfTransactionOutput().orElseThrow());
    }

    private TransactionResult callContractWithoutCatchingException(Address callAddress) {
        byte[] callData = encodeCallData("callDestinationNoExceptionCatching", callAddress);
        return avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice).getTransactionResult();
    }

    private TransactionResult callDestinationAndCatchException(Address callAddress) {
        byte[] callData = encodeCallData("callDestinationAndCatchException", callAddress);
        return avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice).getTransactionResult();
    }

    private byte[] encodeCallData(String methodName, Address address) {
        return ABIUtil.encodeMethodArguments(methodName, address);
    }

    private Address generateDestinationAddressWithSpecifiedFirstByte(byte firstByte) {
        byte[] bytes = Helpers.randomBytes(Address.LENGTH);
        bytes[0] = firstByte;
        return new Address(bytes);
    }

    private void addCodeToAddress(Address address) {
        avmRule.kernel.setTransformedCode(new AionAddress(address.toByteArray()), new byte[1]);
    }

}
