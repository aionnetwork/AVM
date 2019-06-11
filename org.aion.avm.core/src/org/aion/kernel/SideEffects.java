package org.aion.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i.RuntimeAssertionError;
import org.aion.vm.api.types.Address;
import org.aion.vm.api.interfaces.IExecutionLog;
import org.aion.vm.api.interfaces.InternalTransactionInterface;
import org.aion.vm.api.interfaces.TransactionSideEffects;

/**
 * A class representing the side-effects that are caused by executing some external transaction.
 * These side-effects include the following data:
 *
 * 1. All of the logs generated during the execution of this transaction.
 * 2. All of the addressed that were marked to be deleted during the execution of this transaction.
 * 3. All of the internal transactions that were spawned as a result of executing this transaction.
 */
public class SideEffects implements TransactionSideEffects {
    private List<IExecutionLog> logs;
    private List<InternalTransactionInterface> internalTransactions;

    /**
     * Constructs a new empty {@code SideEffects}.
     */
    public SideEffects() {
        this.logs = new ArrayList<>();
        this.internalTransactions = new ArrayList<>();
    }

    @Override
    public void merge(TransactionSideEffects sideEffects) {
        addLogs(sideEffects.getExecutionLogs());
        addInternalTransactions(sideEffects.getInternalTransactions());
    }

    @Override
    public void markAllInternalTransactionsAsRejected() {
        for (InternalTransactionInterface transaction : this.internalTransactions) {
            transaction.markAsRejected();
        }
    }

    @Override
    public void addInternalTransaction(InternalTransactionInterface transaction) {
        this.internalTransactions.add(transaction);
    }

    @Override
    public void addInternalTransactions(List<InternalTransactionInterface> transactions) {
        this.internalTransactions.addAll(transactions);
    }

    @Override
    public void addToDeletedAddresses(Address address) {
        throw new AssertionError("We shouldn't be adding and deleted addresses in the AVM");
    }

    @Override
    public void addAllToDeletedAddresses(Collection<Address> addresses) {
        throw new AssertionError("We shouldn't be adding and deleted addresses in the AVM");
    }

    @Override
    public void addLog(IExecutionLog log) {
        this.logs.add(log);
    }

    @Override
    public void addLogs(Collection<IExecutionLog> logs) {
        this.logs.addAll(logs);
    }

    @Override
    public List<InternalTransactionInterface> getInternalTransactions() {
        return this.internalTransactions;
    }

    @Override
    public List<Address> getAddressesToBeDeleted() {
        return new ArrayList<>();
    }

    @Override
    public List<IExecutionLog> getExecutionLogs() {
        return this.logs;
    }

}
