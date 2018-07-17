package org.aion.avm.core.rejection;


public class RejectMonitorOperations {
    public void doNothing(Object lock) {
        synchronized(lock) {
            // We don't want to allow monitor operations (the contract is single-threaded and this could change thread state).
        }
    }
}
