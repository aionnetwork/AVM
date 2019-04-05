package org.aion.avm.tooling;

import org.aion.avm.core.util.ABIUtil;
import avm.Address;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.TestingKernel;
import org.aion.vm.api.interfaces.TransactionResult;
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
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
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

            if (valueAsByte != TestingKernel.AVM_CONTRACT_PREFIX) {
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

            if (valueAsByte != TestingKernel.AVM_CONTRACT_PREFIX) {
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
        byte[] jar = avmRule.getDappBytes(BadDestinationTarget.class, new byte[0]);

        TransactionResult result = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
        contract = new Address(result.getReturnData());
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
        avmRule.kernel.putCode(org.aion.types.Address.wrap(address.unwrap()), new byte[1]);
    }

}
