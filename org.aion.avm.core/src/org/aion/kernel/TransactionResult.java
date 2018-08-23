package org.aion.kernel;

import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.Helpers;

import java.util.ArrayList;
import java.util.List;

public class TransactionResult {

    public enum  Code {
        /**
         * The transaction was executed successfully.
         */
        SUCCESS(),

        /**
         * This transaction was rejected.
         */
        REJECTED,

        /**
         * Insufficient balance to conduct the transaction.
         */
        REJECTED_INSUFFICIENT_BALANCE,


        /**
         * The transaction nonce does not match the account nonce.
         */
        REJECTED_INVALID_NONCE,

        /**
         * The transaction data is malformed
         */
        REJECTED_INVALID_DATA,


        /**
         * A failure occurred during the execution of the transaction.
         */
        FAILED,

        /**
         * Transaction failed due to out of energy.
         */
        FAILED_OUT_OF_ENERGY,

        /**
         * Transaction failed due to stack overflow.
         */
        FAILED_OUT_OF_STACK,

        /**
         * Transaction failed due to a REVERT operation.
         */
        FAILED_REVERT,

        /**
         * Transaction failed due to an INVALID operation.
         */
        FAILED_INVALID,

        /**
         * Transaction failed due to an uncaught exception.
         */
        FAILED_EXCEPTION;


        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isRejected() {
            return this == REJECTED || this == REJECTED_INSUFFICIENT_BALANCE || this == REJECTED_INVALID_DATA || this == REJECTED_INVALID_NONCE;
        }

        public boolean isFailure() {
            return this == FAILED || this == FAILED_OUT_OF_ENERGY || this == FAILED_REVERT || this == FAILED_INVALID || this == FAILED_EXCEPTION;
        }
    }

    /**
     * The status code.
     */
    private Code statusCode;

    /**
     * The return data.
     */
    private byte[] returnData;

    /**
     * The cumulative energy used.
     */
    private long energyUsed;

    /**
     * The logs emitted during execution.
     */
    private List<Log> logs = new ArrayList<>();

    /**
     * The internal transactions created during execution.
     */
    private List<InternalTransaction> internalTransactions = new ArrayList<>();

    public void merge(TransactionResult other) {
        internalTransactions.addAll(other.getInternalTransactions());

        if (other.statusCode == Code.SUCCESS) {
            logs.addAll(other.getLogs());
        }
    }

    public void addLog(Log log) {
        this.logs.add(log);
    }

    public void addInternalTransaction(InternalTransaction tx) {
        this.internalTransactions.add(tx);
    }

    public TransactionResult() {
        this.statusCode = Code.SUCCESS;
    }

    public Code getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Code statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }

    public long getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(long energyUsed) {
        this.energyUsed = energyUsed;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "statusCode=" + statusCode +
                ", returnData=" + (returnData == null ? "NULL" : Helpers.toHexString(returnData)) +
                ", energyUsed=" + energyUsed +
                '}';
    }
}
