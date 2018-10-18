package org.aion.parallel;

import java.util.PriorityQueue;

/**
 * A package private class represent resource from address.
 *
 * Each {@link AddressResource} keeps a {@link PriorityQueue} of tasks waiting to acquire the resource.
 */

class AddressResource {

    /**
     * Priority queue of {@link TransactionTask}
     * The priority is determined by the index of the transaction.
     * See {@link TransactionTask#compareTo(TransactionTask)}
     */
    private PriorityQueue<TransactionTask> waitingQueue;

    private boolean isOwned;

    private TransactionTask ownedBy;

    AddressResource(){
        this.waitingQueue = new PriorityQueue();
        this.isOwned = false;
        this.ownedBy = null;
    }

    void addToWaitingQueue(TransactionTask task){
        if (!waitingQueue.contains(task)) {
            waitingQueue.add(task);
            if (isNextOwner(task) && null != ownedBy && task != ownedBy) {
                ownedBy.setAbortState();
            }
        }
    }

    void removeFromWaitingQueue(TransactionTask task){
        waitingQueue.remove(task);
    }

    boolean isOwned() {
        return isOwned;
    }

    boolean isNextOwner(TransactionTask task){
        return (task.getIndex() == waitingQueue.peek().getIndex());
    }

    TransactionTask getOwnedBy() {
        return ownedBy;
    }

    void setOwner(TransactionTask task){
        this.ownedBy = task;
        this.isOwned = null == task;
    }

    void setOwned(boolean owned) {
        isOwned = owned;
    }

    void setOwnedBy(TransactionTask ownedBy) {
        this.ownedBy = ownedBy;
    }

    TransactionTask getNextOwner(){
        return this.waitingQueue.peek();
    }
}

