package org.aion.avm.core.rejection;


public class RejectSynchronizedMethod {
    public synchronized void doNothing() {
        // We don't want to allow synchronized methods (the contract is single-threaded and this could change thread state).
    }
}
