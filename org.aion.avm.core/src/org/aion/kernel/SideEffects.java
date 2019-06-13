package org.aion.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aion.types.AionAddress;
import org.aion.vm.api.interfaces.IExecutionLog;
import org.aion.vm.api.interfaces.InternalTransactionInterface;

/**
 * A class representing the side-effects that are caused by executing some external transaction.
 * These side-effects include the following data:
 *
 * 1. All of the logs generated during the execution of this transaction.
 * 2. All of the addressed that were marked to be deleted during the execution of this transaction.
 * 3. All of the internal transactions that were spawned as a result of executing this transaction.
 */
public class SideEffects {
    private List<IExecutionLog> logs;
    private List<InternalTransactionInterface> internalTransactions;

    /**
     * Constructs a new empty {@code SideEffects}.
     */
    public SideEffects() {
        this.logs = new ArrayList<>();
        this.internalTransactions = new ArrayList<>();
    }

    public void merge(SideEffects sideEffects) {
        addLogs(sideEffects.getExecutionLogs());
        addInternalTransactions(sideEffects.getInternalTransactions());
    }

    public void markAllInternalTransactionsAsRejected() {
        for (InternalTransactionInterface transaction : this.internalTransactions) {
            transaction.markAsRejected();
        }
    }

    public void addInternalTransaction(InternalTransactionInterface transaction) {
        this.internalTransactions.add(transaction);
    }

    public void addInternalTransactions(List<InternalTransactionInterface> transactions) {
        this.internalTransactions.addAll(transactions);
    }

    public void addToDeletedAddresses(AionAddress address) {
        throw new AssertionError("We shouldn't be adding and deleted addresses in the AVM");
    }

    public void addAllToDeletedAddresses(Collection<AionAddress> addresses) {
        throw new AssertionError("We shouldn't be adding and deleted addresses in the AVM");
    }

    public void addLog(IExecutionLog log) {
        this.logs.add(log);
    }

    public void addLogs(Collection<IExecutionLog> logs) {
        this.logs.addAll(logs);
    }

    public List<InternalTransactionInterface> getInternalTransactions() {
        return this.internalTransactions;
    }

    public List<AionAddress> getAddressesToBeDeleted() {
        return new ArrayList<>();
    }

    public List<IExecutionLog> getExecutionLogs() {
        return this.logs;
    }

}
