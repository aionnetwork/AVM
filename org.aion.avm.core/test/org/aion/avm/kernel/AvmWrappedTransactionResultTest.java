package org.aion.avm.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import i.RuntimeAssertionError;
import java.util.Collections;
import org.aion.kernel.AvmWrappedTransactionResult;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.TransactionResult;
import org.aion.types.TransactionStatus;
import org.junit.Test;

/**
 * The {@link org.aion.kernel.AvmWrappedTransactionResult} class mostly just wraps a class we've
 * already got tests for -- the purpose of these tests is to ensure that our wrapper class is always
 * in an internally consistent state, since it introduces new error types into the mix.
 */
public class AvmWrappedTransactionResultTest {

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever AvmInternalError.NONE is used, the transaction result must be successful.
    @Test
    public void testInternalErrorNoneAndSuccessfulResult() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(successfulResult(), null, null, AvmInternalError.NONE);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailed());
        assertFalse(result.isAborted());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isRevert());
        assertFalse(result.isRejected());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorNoneAndFailedResultIncompatible() {
        new AvmWrappedTransactionResult(nonRevertedFailedResult(), null, null, AvmInternalError.NONE);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorNoneAndRevertedResultIncompatible() {
        new AvmWrappedTransactionResult(revertedFailedResult(), null, null, AvmInternalError.NONE);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorNoneAndRejectedResultIncompatible() {
        new AvmWrappedTransactionResult(rejectedResult(), null, null, AvmInternalError.NONE);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorNoneAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), null, null, AvmInternalError.NONE);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever AvmInternalError.ABORTED is used, the transaction result must be non-reverted failure.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorAbortedAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), null, null, AvmInternalError.ABORTED);
    }

    @Test
    public void testInternalErrorAbortedAndNonRevertedFailure() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(nonRevertedFailedResult(), null, null, AvmInternalError.ABORTED);
        assertTrue(result.isAborted());
        assertTrue(result.isFailed());
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isRevert());
        assertFalse(result.isRejected());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorAbortedAndRevertedResultIncompatible() {
        new AvmWrappedTransactionResult(revertedFailedResult(), null, null, AvmInternalError.ABORTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorAbortedAndRejectedResultIncompatible() {
        new AvmWrappedTransactionResult(rejectedResult(), null, null, AvmInternalError.ABORTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorAbortedAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), null, null, AvmInternalError.ABORTED);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever AvmInternalError.FAILED_REVERTED is used, the transaction result must be reverted failure.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRevertedAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), null, null, AvmInternalError.FAILED_REVERTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRevertedAndNonRevertedFailureIncompatible() {
        new AvmWrappedTransactionResult(nonRevertedFailedResult(), null, null, AvmInternalError.FAILED_REVERTED);
    }

    @Test
    public void testInternalErrorRevertedAndRevertedResult() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(revertedFailedResult(), null, null, AvmInternalError.FAILED_REVERTED);
        assertTrue(result.isRevert());
        assertTrue(result.isFailed());
        assertFalse(result.isSuccess());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRevertedAndRejectedResultIncompatible() {
        new AvmWrappedTransactionResult(rejectedResult(), null, null, AvmInternalError.FAILED_REVERTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRevertedAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), null, null, AvmInternalError.FAILED_REVERTED);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever a non-reverted failed AvmInternalError is used, the transaction result must be a non-reverted failure.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorFailedAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), null, null, AvmInternalError.FAILED_OUT_OF_ENERGY);
    }

    @Test
    public void testInternalErrorFailedAndNonRevertedFailure() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(nonRevertedFailedResult(), null, null, AvmInternalError.FAILED_OUT_OF_STACK);
        assertTrue(result.isFailed());
        assertFalse(result.isSuccess());
        assertFalse(result.isRevert());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isAborted());
        assertFalse(result.isRejected());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorFailedAndRevertedResult() {
        new AvmWrappedTransactionResult(revertedFailedResult(), null, null, AvmInternalError.FAILED_REJECTED_CLASS);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorFailedAndRejectedResultIncompatible() {
        new AvmWrappedTransactionResult(rejectedResult(), null, null, AvmInternalError.FAILED_INVALID_DATA);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorFailedAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), null, null, AvmInternalError.FAILED_CALL_DEPTH_LIMIT);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever a rejected AvmInternalError is used, the transaction result must be a rejection.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRejectedAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), null, null, AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRejectedAndNonRevertedFailureIncompatible() {
        new AvmWrappedTransactionResult(nonRevertedFailedResult(), null, null, AvmInternalError.REJECTED_INSUFFICIENT_BALANCE);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRejectedAndRevertedResult() {
        new AvmWrappedTransactionResult(revertedFailedResult(), null, null, AvmInternalError.REJECTED_INVALID_ENERGY_PRICE);
    }

    @Test
    public void testInternalErrorRejectedAndRejectedResult() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(rejectedResult(), null, null, AvmInternalError.REJECTED_INVALID_NONCE);
        assertTrue(result.isRejected());
        assertFalse(result.isFailed());
        assertFalse(result.isSuccess());
        assertFalse(result.isRevert());
        assertFalse(result.isFailedException());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isAborted());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorRejectedAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), null, null, AvmInternalError.REJECTED_INVALID_VALUE);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever AvmInternalError.FAILED_EXCEPTION is used, the transaction result must be a non-reverted failure.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorExceptionAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), new NullPointerException(), null, AvmInternalError.FAILED_EXCEPTION);
    }

    @Test
    public void testInternalErrorExceptionAndNonRevertedFailure() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(nonRevertedFailedResult(), new NullPointerException(), null, AvmInternalError.FAILED_EXCEPTION);
        assertTrue(result.isFailedException());
        assertTrue(result.isFailed());
        assertFalse(result.isRejected());
        assertFalse(result.isSuccess());
        assertFalse(result.isRevert());
        assertFalse(result.isFailedUnexpected());
        assertFalse(result.isAborted());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorExceptionAndRevertedResult() {
        new AvmWrappedTransactionResult(revertedFailedResult(), new NullPointerException(), null, AvmInternalError.FAILED_EXCEPTION);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorExceptionAndRejectedResult() {
        new AvmWrappedTransactionResult(rejectedResult(), new NullPointerException(), null, AvmInternalError.FAILED_EXCEPTION);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorExceptionAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), new NullPointerException(), null, AvmInternalError.FAILED_EXCEPTION);
    }
    //<-------------------------------------------------------------------------------------------->

    //<-------------------------------------------------------------------------------------------->
    // The following tests ensure that whenever AvmInternalError.FAILED_UNEXPECTED is used, the transaction result must be a non-reverted failure.
    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorUnexpectedAndSuccessfulResultIncompatible() {
        new AvmWrappedTransactionResult(successfulResult(), new NullPointerException(), null, AvmInternalError.FAILED_UNEXPECTED);
    }

    @Test
    public void testInternalErrorUnexpectedAndNonRevertedFailure() {
        AvmWrappedTransactionResult result = new AvmWrappedTransactionResult(nonRevertedFailedResult(), new NullPointerException(), null, AvmInternalError.FAILED_UNEXPECTED);
        assertTrue(result.isFailedUnexpected());
        assertTrue(result.isFailed());
        assertFalse(result.isRejected());
        assertFalse(result.isSuccess());
        assertFalse(result.isRevert());
        assertFalse(result.isFailedException());
        assertFalse(result.isAborted());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorUnexpectedAndRevertedResult() {
        new AvmWrappedTransactionResult(revertedFailedResult(), new NullPointerException(), null, AvmInternalError.FAILED_UNEXPECTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorUnexpectedAndRejectedResult() {
        new AvmWrappedTransactionResult(rejectedResult(), new NullPointerException(), null, AvmInternalError.FAILED_UNEXPECTED);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testInternalErrorUnexpectedAndFatalResultIncompatible() {
        new AvmWrappedTransactionResult(fatalResult(), new NullPointerException(), null, AvmInternalError.FAILED_UNEXPECTED);
    }
    //<-------------------------------------------------------------------------------------------->

    private static TransactionResult successfulResult() {
        return new TransactionResult(TransactionStatus.successful(), Collections.emptyList(), Collections.emptyList(), 0, null);
    }

    private static TransactionResult nonRevertedFailedResult() {
        return new TransactionResult(TransactionStatus.nonRevertedFailure("failed"), Collections.emptyList(), Collections.emptyList(), 0, null);
    }

    private static TransactionResult revertedFailedResult() {
        return new TransactionResult(TransactionStatus.revertedFailure(), Collections.emptyList(), Collections.emptyList(), 0, null);
    }

    private static TransactionResult rejectedResult() {
        return new TransactionResult(TransactionStatus.rejection("rejected"), Collections.emptyList(), Collections.emptyList(), 0, null);
    }

    private static TransactionResult fatalResult() {
        return new TransactionResult(TransactionStatus.fatal("fatal"), Collections.emptyList(), Collections.emptyList(), 0, null);
    }
}
