package org.aion.parallel;

import org.aion.avm.core.ReentrantDAppStack;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.*;


/**
 * A TransactionTask represent a complete transaction chain started from an external transaction.
 * The purpose of this class is to support asynchronous task abort to achieve concurrency.
 */
public class TransactionTask implements Comparable<TransactionTask>{

    private TransactionContext externalTransactionContext;
    private volatile boolean abortState;
    private IHelper helper;
    private ReentrantDAppStack reentrantDAppStack;
    private int index;
    private KernelInterface taskKernel;
    private StringBuffer outBuffer;

    public TransactionTask(TransactionContext ctx, int index){
        this.externalTransactionContext = ctx;
        this.index = index;
        this.abortState = false;
        this.helper = null;
        this.reentrantDAppStack = new ReentrantDAppStack();
        this.outBuffer = new StringBuffer();
    }

    public TransactionTask(int index, Thread t){
        this.abortState = false;
        this.helper = null;
        this.index = index;
    }

    public void resetState(){
        this.abortState = false;
        this.helper = null;
        this.reentrantDAppStack = new ReentrantDAppStack();
        this.outBuffer = new StringBuffer();
    }

    /**
     * Attach an {@link IHelper} to the current task.
     * If the task is already in abort state, set the helper abort state as well.
     */
    public void attachHelper(IHelper helper) {
        this.helper = helper;
        //TODO: potential broken state
        if (this.abortState){
            helper.externalSetAbortState();
        }
    }

    /**
     * Set the current task state to require abort.
     * If a helper is already attached to this task, set the helper abort state as well.
     */
    public void setAbortState() {
        this.abortState = true;
        //TODO: potential broken state
        if (null != helper){
            helper.externalSetAbortState();
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
     * Get the entry (external) transaction context of the current task.
     *
     * @return The entry (external) transaction context of the task.
     */
    public TransactionContext getExternalTransactionCtx() {
        return externalTransactionContext;
    }

    /**
     * Set the per task transactional kernel of the current task.
     */
    public void setTaskKernel(KernelInterface taskKernel) {
        RuntimeAssertionError.assertTrue(taskKernel instanceof TransactionalKernel);
        this.taskKernel = taskKernel;
    }

    /**
     * Get the per task transactional kernel of the current task.
     *
     * @return The task transactional kernel of the task.
     */
    public KernelInterface getTaskKernel() {
        return taskKernel;
    }

    public void outputPrint(String toPrint){
        this.outBuffer.append(toPrint);
    }

    public void outputPrintln(String toPrint){
        this.outBuffer.append(toPrint + "\n");
    }

    void outputFlush(){
        if (this.outBuffer.length() > 0) {
            System.out.println("Output from transaction " + Helpers.bytesToHexString(externalTransactionContext.getTransactionHash()));
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
