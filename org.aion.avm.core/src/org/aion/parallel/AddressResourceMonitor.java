package org.aion.parallel;

import org.aion.avm.internal.RuntimeAssertionError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Used by executor threads to communicate with each other.
 * Executor threads can only acquire/release {@link AddressResource}, commit result through this monitor.
 * A new monitor will be created for each batch of transactions.
 */
public class AddressResourceMonitor {
    static boolean DEBUG = false;

    // Map for resource retrieval
    private HashMap<AddressWrapper, AddressResource> resources;

    // Ownership records for each task. It provide fast resource release.
    private HashMap<TransactionTask, Set<AddressResource>> ownerships;

    // Private monitor for safety
    private final Object sync;

    // Commit counter used to serialize transaction commit
    private int commitCounter;

    public AddressResourceMonitor()
    {
        this.resources = new HashMap<>();
        this.ownerships = new HashMap<>();
        this.sync = new Object();
        this.commitCounter = 0;
    }


    /**
     * Acquire a resource for given task.
     * Called by executor thread when access of a address is needed.
     *
     * This method block when another executor thread is holding the resource
     *
     * This method return when
     *      The resource is sucessfully acquired (This method support reentrant)
     *      OR
     *      The requester thread need to abort
     *
     * @param address The address requested.
     * @param task The requester task.
     */
    public void acquire(byte[] address, TransactionTask task){
        synchronized (sync) {
            AddressWrapper addressWrapper = new AddressWrapper(address);
            AddressResource resource = getResource(addressWrapper);

            // Add task to the waiting queue.
            if (resource.addToWaitingQueue(task)) {
                sync.notifyAll();
            }

            if (DEBUG) {
                int holder = -1;
                if (resource.getOwnedBy() != null) {
                    holder = resource.getOwnedBy().getIndex();
                }
                int nextOwner = -1;
                if (resource.getNextOwner() != null) {
                    nextOwner = resource.getNextOwner().getIndex();
                }

                System.out.println("Asking " + task.getIndex() + " " + resource.toString() + " hold by " + holder +
                        " nextOwner " + nextOwner + " isNextOwner " + resource.isNextOwner(task) + " locked " +
                        resource.isOwned() + " Abort state " + task.inAbortState());
            }

            // Resource res is granted to task iff
            // res is not hold by other task && task is the next owner
            while ((resource.isOwned() || !resource.isNextOwner(task)) && task != resource.getOwnedBy()
                    && !task.inAbortState()){
                try {
                    sync.wait();
                }catch (InterruptedException e){
                    RuntimeAssertionError.unreachable("Address Resource Lock received Interruption");
                }
            }

            if (!task.inAbortState()) {
                if (DEBUG) System.out.println("Acquire " + task.getIndex() + " " + resource.toString());
                resource.setOwner(task);
                recordOwnership(resource, task);
            }else{
                if (DEBUG) System.out.println("Abort " + task.getIndex() + " " + resource.toString());
            }

            if (DEBUG) System.out.flush();
        }
    }

    /**
     * Release all resource holding by given task.
     * Called by executor thread when the task finished/need restart.
     *
     * This method will not block.
     *
     * @param task The requesting task.
     */
    public void releaseResourcesForTask(TransactionTask task){
        synchronized (sync) {
            for (AddressResource resource : ownerships.remove(task)) {
                this.release(resource, task);
            }

            // Only wake up others when all resources are released
            sync.notifyAll();
        }
    }

    private void release(AddressResource resource, TransactionTask task){
        resource.removeFromWaitingQueue(task);
        if (task == resource.getOwnedBy()) {
            resource.setOwner(null);
        }
        if (DEBUG) System.out.println("Release " + task.getIndex() + " " + resource.toString());
    }

    private AddressResource getResource(AddressWrapper addr){
        AddressResource ret = resources.get(addr);
        if (null == ret){
            ret = new AddressResource();
            resources.put(addr, ret);
        }
        return ret;
    }

    private void recordOwnership(AddressResource res, TransactionTask task){
        Set<AddressResource> entry = ownerships.get(task);
        if (null == entry){
            entry = new HashSet<>();
            ownerships.put(task, entry);
        }
        entry.add(res);
    }

}

