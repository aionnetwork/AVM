package org.aion.parallel;

import org.aion.avm.internal.IHelper;

/**
 * A TransactionTask represent a complete transaction chain started from an external transaction.
 * The purpose of this class is to support asynchronous task abort to achieve concurrency.
 */
public class TransactionTask implements Comparable<TransactionTask>{

    private Thread thread;
    private volatile boolean abortState;
    private IHelper helper;
    private int index;

    public TransactionTask(int index, Thread thread){
        this.thread = thread;
        this.abortState = false;
        this.helper = null;
        this.index = index;
    }

    /**
     * Attach an {@link IHelper} to the current task.
     * If the task is already in abort state, set the helper abort state as well.
     */
    public void attachHelper(IHelper helper) {
        this.helper = helper;
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
     * Get the thread who is executing the task.
     *
     * @return The executor thread of the task.
     */
    public Thread getThread() {
        return thread;
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
