package org.aion.avm.kernel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TransactionResultUtil;
import org.aion.kernel.AvmWrappedTransactionResult;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingState;
import org.aion.types.InternalTransaction;
import org.aion.types.InternalTransaction.RejectedStatus;
import org.aion.types.Log;
import org.junit.Test;

public class TransactionResultUtilTest {
    private static final long ENERGY_USED = 2352634;
    private static final int NUM_LOGS = 4;
    private static final int NUM_INTERNAL_TRANSACTIONS = 6;

    @Test
    public void testNewSuccessfulResultWithEnergyUsedAndOutput() {
        long energyUsed = 314;
        byte[] output = new byte[]{ 0, 1, 1, 2, 0 };

        AvmWrappedTransactionResult result = TransactionResultUtil.newSuccessfulResultWithEnergyUsedAndOutput(energyUsed, output);

        // Verify the status.
        assertEquals(AvmInternalError.NONE, result.avmInternalError);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertArrayEquals(output, result.output());
        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testNewResultWithNonRevertedFailureAndEnergyUsed() {
        AvmInternalError error = AvmInternalError.FAILED_INVALID;
        long energyUsed = 2873;

        AvmWrappedTransactionResult result = TransactionResultUtil.newResultWithNonRevertedFailureAndEnergyUsed(error, energyUsed);

        // Verify the status.
        assertEquals(error, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testNewSuccessfulResultWithEnergyUsed() {
        long energyUsed = 2356732;

        AvmWrappedTransactionResult result = TransactionResultUtil.newSuccessfulResultWithEnergyUsed(energyUsed);

        // Verify the status.
        assertEquals(AvmInternalError.NONE, result.avmInternalError);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testNewRejectedResultWithEnergyUsed() {
        AvmInternalError error = AvmInternalError.REJECTED_INVALID_ENERGY_PRICE;
        long energyUsed = 21376;

        AvmWrappedTransactionResult result = TransactionResultUtil.newRejectedResultWithEnergyUsed(error, energyUsed);

        // Verify the status.
        assertEquals(error, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertTrue(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testNewAbortedResultWithZeroEnergyUsed() {
        AvmWrappedTransactionResult result = TransactionResultUtil.newAbortedResultWithZeroEnergyUsed();

        // Verify the status.
        assertEquals(AvmInternalError.ABORTED, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertTrue(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(0, result.energyUsed());
        assertNull(result.output());
        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testAddLogsAndInternalTransactions() {
        AvmWrappedTransactionResult result = TransactionResultUtil.newSuccessfulResultWithEnergyUsed(0);

        assertTrue(result.logs().isEmpty());
        assertTrue(result.internalTransactions().isEmpty());

        // Add some logs & internal transactions.
        List<Log> logs = randomLogs(5);
        List<InternalTransaction> internalTransactions = randomInternalTransactions(3);

        result = TransactionResultUtil.addLogsAndInternalTransactions(result, logs, internalTransactions);
        assertEquals(5, result.logs().size());
        assertEquals(3, result.internalTransactions().size());
        assertEquals(logs, result.logs());
        assertEquals(internalTransactions, result.internalTransactions());

        // Add some more logs & internal transactions.
        List<Log> otherLogs = randomLogs(7);
        List<InternalTransaction> otherInternalTransactions = randomInternalTransactions(6);

        result = TransactionResultUtil.addLogsAndInternalTransactions(result, otherLogs, otherInternalTransactions);
        assertEquals(5 + 7, result.logs().size());
        assertEquals(3 + 6, result.internalTransactions().size());

        List<Log> expectedLogs = mergeLists(logs, otherLogs);
        List<InternalTransaction> expectedInternalTransactions = mergeLists(internalTransactions, otherInternalTransactions);

        assertEquals(expectedLogs, result.logs());
        assertEquals(expectedInternalTransactions, result.internalTransactions());
    }

    @Test
    public void testAbort() {
        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.abort(result);

        // Verify the status.
        assertEquals(AvmInternalError.ABORTED, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertTrue(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(ENERGY_USED, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testAbortUsingNoEnergy() {
        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.abortUsingNoEnergy(result);

        // Verify the status.
        assertEquals(AvmInternalError.ABORTED, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertTrue(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(0, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetExternalState() {
        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setExternalState(result, new TestingState());

        // Verify the status.
        assertEquals(AvmInternalError.NONE, result.avmInternalError);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(ENERGY_USED, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNotNull(result.externalState);
    }

    @Test
    public void testSetNonRevertedFailureAndEnergyUsed() {
        AvmInternalError error = AvmInternalError.FAILED_REJECTED_CLASS;
        long energyUsed = 982375;

        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setNonRevertedFailureAndEnergyUsed(result, error, energyUsed);

        // Verify the status.
        assertEquals(error, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetRevertedFailureAndEnergyUsed() {
        long energyUsed = 982375;

        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setRevertedFailureAndEnergyUsed(result, energyUsed);

        // Verify the status.
        assertEquals(AvmInternalError.FAILED_REVERTED, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertTrue(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetSuccessfulOutput() {
        byte[] output = new byte[]{ 0, 7, 7, 6, 3, 8 };

        AvmWrappedTransactionResult result = newFailedResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setSuccessfulOutput(result, output);

        // Verify the status.
        assertEquals(AvmInternalError.NONE, result.avmInternalError);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(ENERGY_USED, result.energyUsed());
        assertArrayEquals(output, result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetEnergyUsed() {
        long energyUsed = 982375;

        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setEnergyUsed(result, energyUsed);

        // Verify the status.
        assertEquals(AvmInternalError.NONE, result.avmInternalError);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertNull(result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetFailedException() {
        long energyUsed = 982375;
        Exception exception = new IllegalArgumentException();

        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setFailedException(result, exception, energyUsed);

        // Verify the status.
        assertEquals(AvmInternalError.FAILED_EXCEPTION, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedUnexpected());
        assertTrue(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertEquals(exception, result.exception);
        assertNull(result.externalState);
    }

    @Test
    public void testSetFailedUnexpected() {
        long energyUsed = 235235;
        Exception exception = new IllegalArgumentException();

        AvmWrappedTransactionResult result = newResultWithRandomLogsAndInternalTransactions();
        result = TransactionResultUtil.setFailedUnexpected(result, exception, energyUsed);

        // Verify the status.
        assertEquals(AvmInternalError.FAILED_UNEXPECTED, result.avmInternalError);
        assertFalse(result.isSuccess());
        assertTrue(result.isFailedUnexpected());
        assertFalse(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRevert());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());

        assertEquals(energyUsed, result.energyUsed());
        assertNull(result.output());
        assertEquals(NUM_LOGS, result.logs().size());
        assertEquals(NUM_INTERNAL_TRANSACTIONS, result.internalTransactions().size());
        assertEquals(exception, result.exception);
        assertNull(result.externalState);
    }

    private static AvmWrappedTransactionResult newFailedResultWithRandomLogsAndInternalTransactions() {
        List<Log> logs = randomLogs(NUM_LOGS);
        List<InternalTransaction> internalTransactions = randomInternalTransactions(NUM_INTERNAL_TRANSACTIONS);

        AvmWrappedTransactionResult result = TransactionResultUtil.newResultWithNonRevertedFailureAndEnergyUsed(AvmInternalError.FAILED_INVALID_DATA, ENERGY_USED);
        return TransactionResultUtil.addLogsAndInternalTransactions(result, logs, internalTransactions);
    }

    private static AvmWrappedTransactionResult newResultWithRandomLogsAndInternalTransactions() {
        List<Log> logs = randomLogs(NUM_LOGS);
        List<InternalTransaction> internalTransactions = randomInternalTransactions(NUM_INTERNAL_TRANSACTIONS);

        AvmWrappedTransactionResult result = TransactionResultUtil.newSuccessfulResultWithEnergyUsed(ENERGY_USED);
        return TransactionResultUtil.addLogsAndInternalTransactions(result, logs, internalTransactions);
    }

    private static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        List<T> mergedList = new ArrayList<>(list1);
        mergedList.addAll(list2);
        return mergedList;
    }

    private static List<Log> randomLogs(int num) {
        List<Log> logs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            logs.add(Log.dataOnly(Helpers.randomAddress().toByteArray(), Helpers.randomBytes(10)));
        }
        return logs;
    }

    private static List<InternalTransaction> randomInternalTransactions(int num) {
        List<InternalTransaction> internalTransactions = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            internalTransactions.add(InternalTransaction.contractCreateTransaction(RejectedStatus.NOT_REJECTED, Helpers.randomAddress(), BigInteger.ZERO, BigInteger.ZERO, new byte[0], 0, 1));
        }
        return internalTransactions;
    }
}
