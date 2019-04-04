package org.aion.parallel;

import avm.Address;
import org.aion.avm.core.ReentrantDAppStack;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionInterface;


/**
 * A TransactionTask represent a complete transaction chain started from an external transaction.
 * The purpose of this class is to support asynchronous task abort to achieve concurrency.
 */
public class TransactionTask implements Comparable<TransactionTask>{
    private final KernelInterface parentKernel;
    private TransactionInterface externalTransaction;
    private volatile boolean abortState;
    private IInstrumentation threadOwningTask;
    private ReentrantDAppStack reentrantDAppStack;
    private int index;
    private StringBuffer outBuffer;
    private TransactionalKernel thisTransactionKernel;
    private SideEffects sideEffects;
    private Address origin;
    private int depth;

    public TransactionTask(KernelInterface parentKernel, TransactionInterface tx, int index, org.aion.types.Address origin){
        this.parentKernel = parentKernel;
        this.externalTransaction = tx;
        this.index = index;
        this.abortState = false;
        this.threadOwningTask = null;
        this.reentrantDAppStack = new ReentrantDAppStack();
        this.outBuffer = new StringBuffer();
        this.origin = new Address(origin.toBytes());
        this.depth = 0;
        this.sideEffects = new SideEffects();
    }

    public void startNewTransaction() {
        this.abortState = false;
        this.threadOwningTask = null;
        this.reentrantDAppStack = new ReentrantDAppStack();
        this.outBuffer = new StringBuffer();
        
        // All IO will be performed on an per task transactional kernel so we can abort the whole task in one go
        this.thisTransactionKernel = new TransactionalKernel(this.parentKernel);
    }

    /**
     * Attach an {@link IInstrumentation} to the current task.
     * If the task is already in abort state, set the helper abort state as well.
     */
    public void attachInstrumentationForThread() {
        RuntimeAssertionError.assertTrue(null == this.threadOwningTask);
        this.threadOwningTask = IInstrumentation.attachedThreadInstrumentation.get();
        RuntimeAssertionError.assertTrue(null != this.threadOwningTask);
        //TODO: potential broken state
        if (this.abortState){
            threadOwningTask.setAbortState();
        }
    }

    public void detachInstrumentationForThread() {
        RuntimeAssertionError.assertTrue(IInstrumentation.attachedThreadInstrumentation.get() == this.threadOwningTask);
        this.threadOwningTask = null;
    }

    /**
     * Set the current task state to require abort.
     * If a helper is already attached to this task, set the helper abort state as well.
     */
    public void setAbortState() {
        this.abortState = true;
        //TODO: potential broken state
        if (null != this.threadOwningTask){
            this.threadOwningTask.setAbortState();
        }
    }

    /**
     * Check if the current task requires abort.
     *
     * @return The abort state of the current task.
     */
    public boolean inAbortState(){
        return abortState;
    }

    /**
     * Get the index of the current task.
     *
     * @return The index of the task.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the ReentrantDAppStack of the current task.
     *
     * @return The ReentrantDAppStack of the task.
     */
    public ReentrantDAppStack getReentrantDAppStack() {
        return reentrantDAppStack;
    }

    /**
     * Get the entry (external) transaction of the current task.
     *
     * @return The entry (external) transaction of the task.
     */
    public TransactionInterface getExternalTransaction() {
        return externalTransaction;
    }

    /**
     * Get the per task transactional kernel of the current task.
     *
     * @return The task transactional kernel of the task.
     */
    public TransactionalKernel getThisTransactionalKernel() {
        return this.thisTransactionKernel;
    }

    public void outputPrint(String toPrint){
        this.outBuffer.append(toPrint);
    }

    public void outputPrintln(String toPrint){
        this.outBuffer.append(toPrint + "\n");
    }

    public SideEffects getSideEffects() {
        return sideEffects;
    }

    public Address getOriginAddress() {
        return origin;
    }

    public int getTransactionStackDepth() {
        return depth;
    }

    public void incrementTransactionStackDepth() {
        depth++;
    }

    public void decrementTransactionStackDepth() {
        depth--;
    }

    void outputFlush(){
        if (this.outBuffer.length() > 0) {
            System.out.println("Output from transaction " + Helpers.bytesToHexString(externalTransaction.getTransactionHash()));
            System.out.println(this.outBuffer);
            System.out.flush();
        }
    }

    /**
     * Compare to another task in term of transaction index.
     *
     * The purpose of this method is to support {@link java.util.PriorityQueue}.
     * The lower the index, the higher the priority.
     *
     * @param other Another transaction task.
     * @return The result of the comparision.
     */
    @Override
    public int compareTo(TransactionTask other) {
        int x = this.index;
        int y = other.index;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = this == obj;
        if (!isEqual && (obj instanceof TransactionTask)) {
            TransactionTask other = (TransactionTask) obj;
            isEqual = this.index == other.index;
        }
        return isEqual;
    }
}
