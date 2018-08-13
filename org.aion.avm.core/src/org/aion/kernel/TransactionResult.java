package org.aion.kernel;

import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.Helpers;

import java.util.ArrayList;
import java.util.List;

public class TransactionResult {

    public enum  Code {
        SUCCESS, INVALID_TX, INVALID_JAR, INVALID_CODE, INVALID_CALL, OUT_OF_ENERGY, FAILURE
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
